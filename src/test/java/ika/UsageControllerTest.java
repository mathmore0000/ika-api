package ika;

import com.amazonaws.services.lightsail.model.CreateBucketRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import ika.entities.*;
import ika.entities.aux_classes.auth.SignUpRequest;
import ika.entities.aux_classes.usage.ApproveRejectUsageRequest;
import ika.entities.aux_classes.usage.UsageRequest;
import ika.entities.aux_classes.user_medication.UserMedicationRequest;
import ika.entities.aux_classes.user_medication_stock.UserMedicationStockRequest;
import ika.repositories.*;
import ika.services.UserService;
import ika.utils.JwtUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserResponsibleRepository userResponsibleRepository;

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
    private LabelRepository labelRepository;

    @Autowired
    private UserMedicationStockRepository userMedicationStockRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UsageLabelsRepository usageLabelsRepository;

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

    @Transactional
    @BeforeEach
    void initialize() throws Exception {
        // Implementação da criação de usuário, obtenção de JWT, etc.
        jwt = getJwtFromUser();
        usageLabelsRepository.deleteAll();
        labelRepository.deleteAll();
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
        userId = user.get().getId();
        return jwtUtil.generateToken(userDetails, userId);
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
        System.out.println("1");
        UUID usageId = createUsage();

        // Deletar o usage criado
        System.out.println("2");
        mockMvc.perform(delete("/v1/usages/" + usageId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("Usage deleted successfully"));

        // Confirmar que o usage não existe mais
        System.out.println("3");
        Optional<Usage> deletedUsage = usageRepository.findById(usageId);
        System.out.println("4");
        assert deletedUsage.isEmpty();
    }

    @Test
    void testCreateUsageWithInvalidSolitMedication() throws Exception {
        // Configurar dados do request
        UsageRequest request = new UsageRequest();
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        UserMedication userMedication = userMedicationRepository.findById(userMedicationId).orElseThrow();
        userMedication.getMedication().setQuantityInt(-10);
        userMedication.getMedication().setQuantityMl(0f);
        medicationRepository.save(userMedication.getMedication());

        // Configurar os arquivos do mock
        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Performar o request e verificar a resposta
        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No valid quantity defined for this medication."));
    }

    @Test
    void testCreateUsageWithInvalidLiquidMedication() throws Exception {
        // Configurar dados do request
        UsageRequest request = new UsageRequest();
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        UserMedication userMedication = userMedicationRepository.findById(userMedicationId).orElseThrow();
        userMedication.getMedication().setQuantityInt(0);
        userMedication.getMedication().setQuantityMl(-10f);
        userMedication.setQuantityInt(0);
        userMedication.setQuantityMl(-10f);
        medicationRepository.save(userMedication.getMedication());
        userMedicationRepository.save(userMedication);

        // Configurar os arquivos do mock
        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Performar o request e verificar a resposta
        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No valid quantity defined for this medication."));
    }

    @Test
    void testCreateUsageValidationErrorOnUsageRequest() throws Exception {
        // Configurar dados inválidos do request
        UsageRequest request = new UsageRequest();
        request.setMedications(null); // Deve falhar porque medicamentos são obrigatórios
        request.setActionTmstamp(null); // Deve falhar porque a data é obrigatória

        // Configurar os arquivos do mock
        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Performar o request e verificar a resposta
        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.actionTmstamp").value("Action timestamp is required"))
                .andExpect(jsonPath("$.medications").value("Medications are required"));
    }

    @Test
    void testCreateUsageWithQuantityIntExceedingStock() throws Exception {
        // Configurar dados do request
        UsageRequest request = new UsageRequest();
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        medicationStockRequest.setQuantityInt(1000); // Excedendo o estoque disponível
        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        // Configurar os arquivos do mock
        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Performar o request e verificar a resposta
        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The requested quantityInt exceeds the available stock."));
    }

    @Test
    void testCreateUsageWithQuantityMlExceedingStock() throws Exception {
        UserMedication userMedication = userMedicationRepository.findById(userMedicationId).orElseThrow();
        userMedication.getMedication().setQuantityInt(0);
        userMedication.getMedication().setQuantityMl(10f);
        userMedication.setQuantityMl(10f);
        medicationRepository.save(userMedication.getMedication());
        userMedicationRepository.save(userMedication);

        // Configurar dados do request
        UsageRequest request = new UsageRequest();
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        medicationStockRequest.setQuantityMl(1000f); // Excedendo o estoque disponível
        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        // Configurar os arquivos do mock
        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Performar o request e verificar a resposta
        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The requested quantityMl exceeds the available stock."));
    }

    @Test
    void testCreateUsageWithInvalidQuantityInt() throws Exception {
        // Configurar dados do request
        UsageRequest request = new UsageRequest();
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        medicationStockRequest.setQuantityInt(-1); // Valor inválido para quantidade int
        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        // Configurar os arquivos do mock
        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Performar o request e verificar a resposta
        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The quantityInt must be greater than 0 for solid medications."));
    }

    @Test
    void testCreateUsageExceedsStock() throws Exception {
        // Atualizar estoque para uma quantidade limitada
        UserMedicationStock userMedicationStock = userMedicationStockRepository.findById(userMedicationStockId).orElseThrow();
        userMedicationStock.setQuantityStocked(1); // Definindo um estoque baixo, por exemplo, apenas 1 unidade de 10 comprimidos
        userMedicationStockRepository.save(userMedicationStock);
        UserMedication userMedication = userMedicationRepository.findById(userMedicationId).orElseThrow();
        userMedication.setQuantityInt(10); // Definindo uma quantidade por estoque baixo, 10 comprimidos
        userMedicationRepository.save(userMedication);


        // Criar um `UsageRequest` que excede a quantidade disponível
        UsageRequest request = new UsageRequest();
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        medicationStockRequest.setQuantityInt(15); // Excedendo o estoque, só temos 10 disponíveis
        medicationStockRequest.setQuantityMl(null);
        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Esperar que o servidor retorne um erro indicando que a quantidade excede o estoque
        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The requested quantityInt exceeds the available stock."));
    }

    @Test
    void testCreateUsageForSolidMedicationWithInvalidQuantity() throws Exception {
        // Criar um `UsageRequest` para um medicamento sólido com `quantityMl`, que é inválido
        UserMedication userMedication = userMedicationRepository.findById(userMedicationId).orElseThrow();
        userMedication.getMedication().setQuantityInt(10); // Definir o medicamento como sólido
        userMedication.getMedication().setQuantityMl(0f);
        userMedicationRepository.save(userMedication);

        UsageRequest request = new UsageRequest();
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        medicationStockRequest.setQuantityInt(0);
        medicationStockRequest.setQuantityMl(5.0f); // Passando quantidade ml para um medicamento sólido
        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Esperar que o servidor retorne um erro indicando que a quantidade está incorreta
        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The quantityInt must be greater than 0 for solid medications."));
    }

    @Test
    void testCreateUsageForLiquidMedicationWithInvalidQuantity() throws Exception {
        // Criar um `UsageRequest` para um medicamento líquido com uma quantidade inválida
        UserMedication userMedication = userMedicationRepository.findById(userMedicationId).orElseThrow();
        userMedication.getMedication().setQuantityInt(0);
        userMedication.getMedication().setQuantityMl(10.0f); // Definir o medicamento como líquido
        medicationRepository.save(userMedication.getMedication());

        UsageRequest request = new UsageRequest();
        UsageRequest.MedicationStockRequest medicationStockRequest = new UsageRequest.MedicationStockRequest();
        medicationStockRequest.setMedicationStockId(userMedicationStockId);
        medicationStockRequest.setQuantityInt(null);
        medicationStockRequest.setQuantityMl(-5.0f); // Passando quantidade ml negativa
        request.setMedications(List.of(medicationStockRequest));
        request.setActionTmstamp(LocalDateTime.now());

        MockMultipartFile file = new MockMultipartFile("file", "dummy-video.mp4", "video/mp4", "dummy content".getBytes());
        MockMultipartFile usageRequestPart = new MockMultipartFile("usageRequest", "usageRequest", "application/json", objectMapper.writeValueAsString(request).getBytes());

        // Esperar que o servidor retorne um erro indicando que a quantidade não pode ser negativa
        mockMvc.perform(multipart("/v1/usages")
                        .file(file)
                        .file(usageRequestPart)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The quantityMl must be greater than 0 for liquid medications."));
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

    @Test
    void testApproveUsageNotResponsible() throws Exception {
        // Criar um Usage
        UUID usageId = createUsage();

        // Criar uma label e salvar no banco
        UUID labelId = createLabel("Tomou em pé");

        // Não criar o responsável, para que o teste falhe
        ApproveRejectUsageRequest approveRequest = new ApproveRejectUsageRequest();
        approveRequest.setLabels(List.of(labelId));  // Adicionar a label criada
        approveRequest.setObs("Aprovado");

        // Tentar aprovar o Usage sem ser responsável
        mockMvc.perform(post("/v1/usages/" + usageId + "/approve")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You are not responsible by this usage."));
    }

    @Test
    void testRejectUsageNotResponsible() throws Exception {
        // Criar um Usage
        UUID usageId = createUsage();

        // Criar uma label e salvar no banco
        UUID labelId = createLabel("Tomou em pé");

        // Não criar o responsável, para que o teste falhe
        ApproveRejectUsageRequest rejectRequest = new ApproveRejectUsageRequest();
        rejectRequest.setLabels(List.of(labelId));
        rejectRequest.setObs("Rejeitado");

        // Tentar rejeitar o Usage sem ser responsável
        mockMvc.perform(post("/v1/usages/" + usageId + "/reject")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You are not responsible by this usage."));
    }

    private UUID createLabel(String labelDescription) {
        Label label = new Label();
        label.setId(UUID.randomUUID());
        label.setDescription(labelDescription);
        return labelRepository.save(label).getId();
    }

    private void createResponsibleForUser(UUID userId) {
        UserResponsible responsible = new UserResponsible();
        responsible.setUserId(userId);
        responsible.setResponsibleId(userId);  // O usuário logado será o responsável
        responsible.setAccepted(true);
        userResponsibleRepository.save(responsible);
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
        UUID usageId = createUsage();
        Usage usage = usageRepository.findById(usageId).get();
        usage.setIsApproved(true);
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
