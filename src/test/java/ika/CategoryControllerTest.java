package ika;

import com.fasterxml.jackson.databind.ObjectMapper;
import ika.entities.aux_classes.auth.SignUpRequest;
import ika.entities.Category;
import ika.entities.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use the test profile
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
        categoryRepository.deleteAll();
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
    void testGetCategoryByIdSuccess() throws Exception {
        // Create and save a category entity to the database first
        Category category = new Category(UUID.randomUUID(), "Analgesic");
        category = categoryRepository.save(category);

        // Perform the GET request to retrieve the category by ID
        mockMvc.perform(get("/v1/categories/" + category.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Analgesic"));
    }

    @Test
    void testGetAllCategoriesSuccess() throws Exception {
        // Create and save multiple categories
        categoryRepository.save(new Category(UUID.randomUUID(), "Antibiotic"));
        categoryRepository.save(new Category(UUID.randomUUID(), "Analgesic"));

        // Perform the GET request to retrieve all categories
        mockMvc.perform(get("/v1/categories")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetCategoryByIdNotFound() throws Exception {
        // Create a random UUID for a non-existent category
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/v1/categories/" + nonExistentId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllCategoriesWithFilterAndPagination() throws Exception {
        // Create and save multiple categories
        categoryRepository.save(new Category(UUID.randomUUID(), "Broncodilatador"));
        categoryRepository.save(new Category(UUID.randomUUID(), "Cardioprotetor"));
        categoryRepository.save(new Category(UUID.randomUUID(), "Fitoterápico"));

        // Perform the GET request with filter and pagination
        mockMvc.perform(get("/v1/categories")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].description").value("Broncodilatador"));


        mockMvc.perform(get("/v1/categories")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].description").value("Fitoterápico"));
    }
}
