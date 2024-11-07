package ika;

import com.fasterxml.jackson.databind.ObjectMapper;
import ika.entities.aux_classes.auth.SignUpRequest;
import ika.entities.ActiveIngredient;
import ika.entities.User;
import ika.repositories.ActiveIngredientRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use the test profile
class ActiveIngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private ActiveIngredientRepository activeIngredientRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwt;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

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
        // Create a default user and generate a JWT token before each test
        jwt = getJwtFromUser();
        medicationRepository.deleteAll();
        activeIngredientRepository.deleteAll();
    }

    // Create a user and generate a JWT token
    private String getJwtFromUser() throws Exception {
        String email = "default_user@ika.com";
        createUser("Default User", email, "password", "pt");
        final Optional<User> user = userService.loadUserByEmail(email);
        final UserDetails userDetails = userService.loadUserByUsername(email);
        return jwtUtil.generateToken(userDetails, user.get());
    }

    // Method to create a new user for testing purposes
    void createUser(String displayName, String email, String password, String locale) throws Exception {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            System.out.println("Creating user...");
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
    void testGetActiveIngredientByIdSuccess() throws Exception {
        // Create and save an active ingredient entity to the database first
        ActiveIngredient activeIngredient = new ActiveIngredient(UUID.randomUUID(), "Ibuprofen");
        activeIngredient = activeIngredientRepository.save(activeIngredient);

        // Perform the GET request to retrieve the active ingredient by ID
        mockMvc.perform(get("/v1/active-ingredients/" + activeIngredient.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Ibuprofen"));
    }

    @Test
    void testGetAllActiveIngredientsSuccess() throws Exception {
        // Create and save multiple active ingredients
        activeIngredientRepository.save(new ActiveIngredient(UUID.randomUUID(), "Ibuprofen"));
        activeIngredientRepository.save(new ActiveIngredient(UUID.randomUUID(), "Aspirin"));

        // Perform the GET request to retrieve all active ingredients
        mockMvc.perform(get("/v1/active-ingredients")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetActiveIngredientByIdNotFound() throws Exception {
        // Create a random UUID for a non-existent active ingredient
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/v1/active-ingredients/" + nonExistentId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllActiveIngredientsWithFilterAndPagination() throws Exception {
        // Create and save multiple active ingredients
        activeIngredientRepository.save(new ActiveIngredient(UUID.randomUUID(), "Acetaminophen"));
        activeIngredientRepository.save(new ActiveIngredient(UUID.randomUUID(), "Aspirin"));
        activeIngredientRepository.save(new ActiveIngredient(UUID.randomUUID(), "Ibuprofen"));

        // Perform the GET request with filter and pagination
        mockMvc.perform(get("/v1/active-ingredients")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].description").value("Acetaminophen"));


        mockMvc.perform(get("/v1/active-ingredients")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].description").value("Ibuprofen"));
    }
}
