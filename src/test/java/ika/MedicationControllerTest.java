package ika;

import com.fasterxml.jackson.databind.ObjectMapper;
import ika.controllers.aux_classes.auth.SignUpRequest;
import ika.controllers.aux_classes.medication.MedicationRequest;
import ika.entities.ActiveIngredient;
import ika.entities.Category;
import ika.entities.Medication;
import ika.entities.User;
import ika.repositories.ActiveIngredientRepository;
import ika.repositories.CategoryRepository;
import ika.repositories.MedicationRepository;
import ika.repositories.UserRepository;
import ika.services.UserService;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use the test profile
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActiveIngredientRepository activeIngredientRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwt; // Declare JWT as a class variable

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.1")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // Configure dynamic properties for PostgreSQL container
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
        // Create a default user and generate a JWT token before each test
        jwt = getJwtFromUser();
        medicationRepository.deleteAll();
        categoryRepository.deleteAll();
        activeIngredientRepository.deleteAll();
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

    private Category getCategoryId(String description) {
        Category category = categoryRepository.findByDescription(description)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setDescription(description);
                    return categoryRepository.save(newCategory);
                });
        return category;
    }

    private ActiveIngredient getActiveIngredientId(String description) {
        ActiveIngredient ingredient = activeIngredientRepository.findByDescription(description)
                .orElseGet(() -> {
                    ActiveIngredient newIngredient = new ActiveIngredient();
                    newIngredient.setDescription(description);
                    return activeIngredientRepository.save(newIngredient);
                });
        return ingredient;
    }

    @Test
    void testCreateMedicationSuccess() throws Exception {
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setName("Paracetamol");
        medicationRequest.setDosage(500);
        medicationRequest.setActiveIngredientId(getActiveIngredientId("Paracetamol").getId());
        medicationRequest.setCategoryId(getCategoryId("Antibiótico").getId());
        medicationRequest.setBand(3);
        medicationRequest.setTimeBetween(6);
        medicationRequest.setMaxValidationTime(12);

        mockMvc.perform(post("/v1/medications")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Medication created successfully"));
    }

    @Test
    void testGetMedicationByIdSuccess() throws Exception {
        // Create and save a medication entity to the database first
        Medication medication = new Medication();
        medication.setId(UUID.randomUUID());
        medication.setName("Ibuprofen");
        medication.setDosage(400);
        medication.setBand(3);
        medication.setActiveIngredient(getActiveIngredientId("Clopidogrel"));
        medication.setCategory(getCategoryId("Antifúngico"));
        medication.setTimeBetween(8);
        medication.setMaxValidationTime(24);
        medication = medicationRepository.save(medication);

        // Perform the GET request to retrieve the medication by ID
        mockMvc.perform(get("/v1/medications/" + medication.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ibuprofen"))
                .andExpect(jsonPath("$.dosage").value(400));
    }

    @Test
    void testGetMedicationByIdNotFound() throws Exception {
        // Create a random UUID for a non-existent medication
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/v1/medications/" + nonExistentId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllMedicationsWithFilterAndPagination() throws Exception {
        // Create and save related entities: category and active ingredient
        Category category = categoryRepository.save(new Category(UUID.randomUUID(), "Analgesic"));
        ActiveIngredient activeIngredient = activeIngredientRepository.save(new ActiveIngredient(UUID.randomUUID(), "Paracetamol"));

        // Create and save multiple medications with different categories and active ingredients
        medicationRepository.save(new Medication(
                UUID.randomUUID(),
                "Ibuprofen 400mg",
                false,
                3,
                10F,
                activeIngredient,
                category,
                400,
                30,
                null,  // id_user, se aplicável, substitua por um valor válido
                true,
                24,
                8,
                30,   // quantityInt, porque é um medicamento sólido
                null  // quantityMl, porque é um medicamento sólido
        ));

        medicationRepository.save(new Medication(
                UUID.randomUUID(),
                "Paracetamol 500mg",
                false,
                3,
                10F,
                activeIngredient,
                category,
                500,
                20,
                null,  // id_user, se aplicável, substitua por um valor válido
                true,
                24,
                8,
                20,   // quantityInt, porque é um medicamento sólido
                null  // quantityMl, porque é um medicamento sólido
        ));

        medicationRepository.save(new Medication(
                UUID.randomUUID(),
                "Paracetamol 750mg",
                false,
                3,
                10F,
                activeIngredient,
                category,
                750,
                15,
                null,  // id_user, se aplicável, substitua por um valor válido
                true,
                24,
                8,
                15,   // quantityInt, porque é um medicamento sólido
                null  // quantityMl, porque é um medicamento sólido
        ));

        // Perform the GET request with filter and pagination
        mockMvc.perform(get("/v1/medications")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Ibuprofen 400mg"))
                .andExpect(jsonPath("$.content[1].name").value("Paracetamol 500mg"));

        // Perform the GET request for the second page
        mockMvc.perform(get("/v1/medications")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Paracetamol 750mg"));
    }
}
