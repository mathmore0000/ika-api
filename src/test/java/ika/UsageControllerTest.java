package ika;

import com.amazonaws.services.lightsail.model.CreateBucketRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import ika.entities.*;
import ika.entities.aux_classes.auth.SignUpRequest;
import ika.entities.aux_classes.usage.UsageRequest;
import ika.entities.aux_classes.user_medication.UserMedicationRequest;
import ika.entities.aux_classes.user_medication_stock.UserMedicationStockRequest;
import ika.repositories.*;
import ika.services.UserService;
import ika.utils.JwtUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsageRepository usageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwt;
    private UUID userId;
    private UUID medicationId;
    private UUID userMedicationId;
    private UUID userMedicationStockId;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserMedicationRepository userMedicationRepository;

    @Autowired
    private UserMedicationStockRepository userMedicationStockRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private UserMedicationStockUsageRepository userMedicationStockUsageRepository;

    @Autowired
    private AmazonS3 s3Client;  // Cliente S3 injetado, configurado pelo LocalStack

    @Autowired
    private ActiveIngredientRepository activeIngredientRepository;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.1")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void setup() {
        postgres.start();
    }

    @BeforeEach
    void initialize() throws Exception {
        // Implementação da criação de usuário, obtenção de JWT, etc.
        jwt = getJwtFromUser();
        userMedicationStockUsageRepository.deleteAll();
        usageRepository.deleteAll();
        fileRepository.deleteAll();
        userMedicationStockRepository.deleteAll();
        userMedicationRepository.deleteAll();
        medicationRepository.deleteAll();
        activeIngredientRepository.deleteAll();
        categoryRepository.deleteAll();
        bucketRepository.deleteAll();
        medicationId = createMedication("Dipirona 500mg", "Dipirona", "Analgésico");
        userMedicationId = createUserMedication(medicationId);
        userMedicationStockId = createUserMedicationStock(userMedicationId);
        Bucket bucket = new Bucket();
        bucket.setDescription("videos");
        String bucket_name = bucketRepository.save(bucket).getName();
        s3Client.createBucket(bucket_name);
    }

    // Create a user and generate a JWT token
    private String getJwtFromUser() throws Exception {
        String email = "default_user@ika.com";
        createUser("Default User", email, "password", "pt");
        final Optional<User> user = userService.loadUserByEmail(email);
        final UserDetails userDetails = userService.loadUserByUsername(email);
        return jwtUtil.generateToken(userDetails, user.get().getId());
    }

    // Method to create a new user for testing purposes
    void createUser(String displayName, String email, String password, String locale) throws Exception {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            System.out.println("criando usuário...");
            SignUpRequest signUpRequest = new SignUpRequest();
            signUpRequest.setDisplayName(displayName);
            signUpRequest.setEmail(email);
            signUpRequest.setPassword(password);
            signUpRequest.setLocale(locale);
            mockMvc.perform(post("/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User registered successfully"));

        }
    }

    @Test
    void testCreateUsageSuccess() throws Exception {
        UsageRequest request = new UsageRequest();

        // Setting up valid data for medications in usageRequest
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        medicationStockRequest.setQuantityInt(2);
        medicationStockRequest.setQuantityMl(null);

        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)  // Adding `usageRequest` JSON part
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usage created successfully"));
    }


    @Test
    void testCreateUsageMissingUsageRequest() throws Exception {
        mockMvc.perform(multipart("/v1/usages")
                        .file("file", "dummy content".getBytes())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing part: usageRequest"));
    }

    @Test
    void testCreateUsageMissingUsageFile() throws Exception {
        UsageRequest request = new UsageRequest();
        mockMvc.perform(multipart("/v1/usages")
                        .param("usageRequest", objectMapper.writeValueAsString(request)) // Send `usageRequest` as JSON in form-data
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing part: file"));
    }

    @Test
    void testDeleteUsageSuccess() throws Exception {
        // Primeiro criar um Usage
        UUID usageId = createUsage();

        // Deletar o usage criado
        mockMvc.perform(delete("/v1/usages/" + usageId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("Usage deleted successfully"));

        // Confirmar que o usage não existe mais
        Optional<Usage> deletedUsage = usageRepository.findById(usageId);
        assert deletedUsage.isEmpty();
    }

    @Test
    void testDeleteUsageNotApproved() throws Exception {
        // Primeiro criar um Usage que já esteja aprovado
        UUID usageId = createApprovedUsage();

        // Tentar deletar o usage aprovado
        mockMvc.perform(delete("/v1/usages/" + usageId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usage cannot be deleted because it has already been approved"));
    }

    @Test
    void testGetFilteredUsages() throws Exception {
        createUsage(); // Criar alguns usos

        // Fazer uma requisição GET com filtros específicos
        mockMvc.perform(get("/v1/usages")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    private UUID createUsage() throws Exception {
        UsageRequest request = new UsageRequest();

        // Setting up valid data for medications in usageRequest
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        medicationStockRequest.setQuantityInt(2);
        medicationStockRequest.setQuantityMl(null);

        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Perform the request and capture the result
        String responseContent = mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)  // Adding `usageRequest` JSON part
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usage created successfully"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Parse the response JSON to extract the usageId
        Map<String, String> responseMap = objectMapper.readValue(responseContent, Map.class);
        String usageIdStr = responseMap.get("usageId");

        // Convert the usageId to UUID and return
        return UUID.fromString(usageIdStr);
    }

    // Helper para criar um usage aprovado
    private UUID createApprovedUsage() throws Exception {
        Usage usage = new Usage();
        usage.setId(UUID.randomUUID());
        usage.setIsApproved(true);
        // Preencher outros campos do Usage
        return usageRepository.save(usage).getId();
    }

    private UUID createUserMedication(UUID medicationId) throws Exception {
        UserMedicationRequest request = new UserMedicationRequest();
        request.setIdMedication(medicationId);
        request.setFirstDosageTime(LocalDateTime.now());
        request.setMaxValidationTime(8.0f);

        // Perform the POST request and capture the response
        String response = mockMvc.perform(post("/v1/user-medications")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assuming the response contains the userMedicationId, parse the response to extract it
        // You might need to adjust based on the actual structure of your response.
        UserMedication userMedication = objectMapper.readValue(response, UserMedication.class);
        // Return the userMedicationId
        return userMedication.getId(); // Assuming `getId()` returns the userMedicationId
    }

    private UUID createUserMedicationStock(UUID userMedicationId) throws Exception {
        // Create the stock request object
        UserMedicationStockRequest stockRequest = new UserMedicationStockRequest();
        stockRequest.setUserMedicationId(userMedicationId);
        stockRequest.setQuantityStocked(10);
        stockRequest.setExpirationDate(LocalDateTime.now().plusDays(30)); // Set expiration date 30 days ahead

        // Perform the POST request to create the stock and capture the response
        String response = mockMvc.perform(post("/v1/user-medication-stocks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assuming the response contains the UserMedicationStock object, parse the response to extract it
        UserMedicationStock userMedicationStock = objectMapper.readValue(response, UserMedicationStock.class);

        // Return the created UserMedicationStock ID
        return userMedicationStock.getId(); // Assuming `getId()` returns the userMedicationStock ID
    }


    // Helper method to create a medication
    private UUID createMedication(String name, String activeIngredient, String category) {
        return medicationRepository.save(new Medication(
                UUID.randomUUID(),
                name,
                false,
                3,
                10F,
                activeIngredientRepository.save(new ActiveIngredient(UUID.randomUUID(), activeIngredient)),
                categoryRepository.save(new Category(UUID.randomUUID(), category)),
                750,
                null,
                true,
                24,
                8,
                15,
                null
        )).getId();
    }
}
