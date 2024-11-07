package ika;

import com.fasterxml.jackson.databind.ObjectMapper;
import ika.entities.aux_classes.auth.SignUpRequest;
import ika.entities.aux_classes.user_medication.UserMedicationRequest;
import ika.entities.aux_classes.user_medication_stock.UserMedicationStockRequest;
import ika.entities.*;
import ika.repositories.*;
import ika.services.UserService;
import ika.utils.CurrentUserProvider;
import ika.utils.JwtUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserMedicationStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMedicationRepository userMedicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private UserMedicationStockRepository userMedicationStockRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ActiveIngredientRepository activeIngredientRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    private String jwt;
    private UUID userId;
    private UUID medicationId;
    private UUID userMedicationId;

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
        userMedicationStockRepository.deleteAll();
        userMedicationRepository.deleteAll();
        medicationRepository.deleteAll();
        userRepository.deleteAll();
        activeIngredientRepository.deleteAll();
        categoryRepository.deleteAll();

        // Cria usuário padrão e obtém o JWT
        String email = "test_user@ika.com";
        createUser("Test User", email, "password", "en");
        userId = userRepository.findByEmail(email).get().getId();
        jwt = getJwtFromUser(email);

        medicationId = createMedication("Dipirona 500mg", "Dipirona", "Analgésico");
        userMedicationId = createUserMedication(medicationId);
    }

    private String getJwtFromUser(String email) throws Exception {
        final Optional<User> user = userService.loadUserByEmail(email);
        final UserDetails userDetails = userService.loadUserByUsername(email);
        return jwtUtil.generateToken(userDetails, user.get());
    }

    // Cria um novo usuário para testes
    void createUser(String displayName, String email, String password, String locale) throws Exception {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
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
    void testCreateUserMedicationStockSuccess() throws Exception {
        // Create the request object
        UserMedicationStockRequest request = new UserMedicationStockRequest();
        request.setUserMedicationId(userMedicationId);
        request.setQuantityStocked(10);
        request.setExpirationDate(OffsetDateTime.now().plusDays(30));

        // Send the request as JSON
        mockMvc.perform(post("/v1/user-medication-stocks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))  // Send as JSON in body
                .andExpect(status().isCreated());
    }

    @Test
    void testGetStockForUserMedicationWithPaging() throws Exception {
        createUserMedicationStock(userMedicationId);
        createUserMedicationStock(userMedicationId);

        mockMvc.perform(get("/v1/user-medication-stocks/{medicationId}", medicationId)
                        .param("page", "0")
                        .param("size", "1")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "asc")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void testGetUserMedicationStockNoReturn() throws Exception {
        UUID medicationId = UUID.randomUUID();  // Use a random UUID to ensure no results

        // Perform the GET request for a non-existent medication stock
        mockMvc.perform(get("/v1/user-medication-stocks/" + medicationId)
                        .header("Authorization", "Bearer " + jwt)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())  // Expect the content to be empty
                .andExpect(jsonPath("$.totalPages").value(0));  // There should be 0 pages
    }

    @Test
    void testDeleteUserMedicationStockSuccess() throws Exception {
        UUID userMedicationStockId = createUserMedicationStock(userMedicationId);

        mockMvc.perform(delete("/v1/user-medication-stocks/" + userMedicationStockId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());
    }

    private UUID createUserMedication(UUID medicationId) throws Exception {
        UserMedicationRequest request = new UserMedicationRequest();
        request.setIdMedication(medicationId);
        request.setFirstDosageTime(OffsetDateTime.now());
        request.setMaxTakingTime(8.0f);

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
        stockRequest.setExpirationDate(OffsetDateTime.now().plusDays(30)); // Set expiration date 30 days ahead

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
