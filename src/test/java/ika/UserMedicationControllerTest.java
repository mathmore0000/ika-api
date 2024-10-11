package ika;

import com.fasterxml.jackson.databind.ObjectMapper;
import ika.controllers.aux_classes.auth.SignUpRequest;
import ika.controllers.aux_classes.user_medication.UserMedicationRequest;
import ika.entities.ActiveIngredient;
import ika.entities.Category;
import ika.entities.Medication;
import ika.entities.User;
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
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserMedicationControllerTest {

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
        userMedicationRepository.deleteAll();
        medicationRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        activeIngredientRepository.deleteAll();

        // Create default user and get JWT token
        String email = "test_user@ika.com";
        createUser("Test User", email, "password", "en");
        jwt = getJwtFromUser(email);
        userId = userRepository.findByEmail(email).get().getId();
    }

    private String getJwtFromUser(String email) throws Exception {
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
    void testCreateUserMedicationSuccess() throws Exception {
        UUID medicationId = createMedication("Test Medication", "Dipirona", "Analgésico");

        UserMedicationRequest request = new UserMedicationRequest();
        request.setIdMedication(medicationId);
        request.setFirstDosageTime(LocalDateTime.now());
        request.setMaxValidationTime(8.0f);

        mockMvc.perform(post("/v1/user-medications")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateUserMedicationConflict() throws Exception {
        UUID medicationId = createMedication("Test Medication", "Dipirona", "Analgésico");

        UserMedicationRequest request = new UserMedicationRequest();
        request.setIdMedication(medicationId);
        request.setFirstDosageTime(LocalDateTime.now());
        request.setMaxValidationTime(8.0f);

        // Create initial medication
        mockMvc.perform(post("/v1/user-medications")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try to create the same medication again
        mockMvc.perform(post("/v1/user-medications")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateUserMedicationStatusSuccess() throws Exception {
        UUID userMedicationId = createUserMedication();
        mockMvc.perform(patch("/v1/user-medications/" + userMedicationId + "/status")
                        .param("disabled", "true")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("User medication status updated successfully"));
    }

    @Test
    void testUpdateUserMedicationStatusNotFound() throws Exception {
        mockMvc.perform(patch("/v1/user-medications/" + UUID.randomUUID() + "/status")
                        .param("disabled", "true")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User medication not found"));
    }

    @Test
    void testGetAllUserMedicationsWithFilteringAndPagingSortedDescending() throws Exception {
    }

    @Test
    void testUpdateUserMedicationSuccess() throws Exception {
        UUID userMedicationId = createUserMedication();

        UserMedicationRequest updatedRequest = new UserMedicationRequest();
        updatedRequest.setIdMedication(UUID.randomUUID());
        updatedRequest.setFirstDosageTime(LocalDateTime.now().plusHours(1));
        updatedRequest.setMaxValidationTime(10.0f);
        updatedRequest.setTimeBetween(12.0f);
        updatedRequest.setQuantityInt(10);
        updatedRequest.setQuantityMl(5.0f);
        updatedRequest.setQuantityCard(30);

        mockMvc.perform(put("/v1/user-medications/" + userMedicationId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUserMedicationWithDefaults() throws Exception {
        UUID medicationId = createMedication("Default Test Medication", "Dipirona", "Analgésico");

        UserMedicationRequest request = new UserMedicationRequest();
        request.setIdMedication(medicationId);
        request.setFirstDosageTime(LocalDateTime.now());

        mockMvc.perform(post("/v1/user-medications")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maxValidationTime").value(24.0))  // Verify default value from Medication
                .andExpect(jsonPath("$.timeBetween").value(8.0))  // Verify default value from Medication
                .andExpect(jsonPath("$.quantityCard").value(15))  // Verify default value from Medication
                .andExpect(jsonPath("$.quantityMl").doesNotExist())  // Verify no default for ML because it's a solid medication
                .andExpect(jsonPath("$.quantityInt").value(15));  // Verify default value from Medication
    }

    // Helper method to create a user medication
    private UUID createUserMedication() throws Exception {
        UUID medicationId = createMedication("Ibuprofeno", "Ibuprofeno", "Anti-inflamatório");

        UserMedicationRequest request = new UserMedicationRequest();
        request.setIdMedication(medicationId);
        request.setFirstDosageTime(LocalDateTime.now());
        request.setMaxValidationTime(8.0f);

        mockMvc.perform(post("/v1/user-medications")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        return medicationId;
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
                15,
                null,  // id_user, se aplicável, substitua por um valor válido
                true,
                24,
                8,
                15,   // quantityInt, porque é um medicamento sólido
                null  // quantityMl, porque é um medicamento sólido
        )).getId();
    }
}