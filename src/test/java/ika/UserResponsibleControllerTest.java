package ika;

import com.fasterxml.jackson.databind.ObjectMapper;
import ika.controllers.aux_classes.auth.SignUpRequest;
import ika.entities.User;
import ika.entities.UserResponsible;
import ika.repositories.UserRepository;
import ika.repositories.UserResponsibleRepository;
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
@ActiveProfiles("test")
class UserResponsibleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserResponsibleRepository userResponsibleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwt;

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
        userResponsibleRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String getJwtFromUser() throws Exception {
        String email = "default_user@ika.com";
        createUser("Default User", email, "password", "pt");
        final Optional<User> user = userService.loadUserByEmail(email);
        final UserDetails userDetails = userService.loadUserByUsername(email);
        return jwtUtil.generateToken(userDetails, user.get().getId());
    }

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
    void testCreateRequestSuccess() throws Exception {
        // Create a new responsible user to be associated with the request
        UUID responsibleId = createResponsibleUser("Responsible User", "responsible_user@ika.com");

        // Perform the POST request to create a responsible request
        mockMvc.perform(post("/v1/responsibles")
                        .param("idResponsible", responsibleId.toString())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateRequestDuplicate() throws Exception {
        UUID responsibleId = createResponsibleUser("Responsible User", "responsible_user@ika.com");

        // Create an initial request
        mockMvc.perform(post("/v1/responsibles")
                        .param("idResponsible", responsibleId.toString())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // Try to create the same request again to verify conflict
        mockMvc.perform(post("/v1/responsibles")
                        .param("idResponsible", responsibleId.toString())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void testAcceptRequestSuccess() throws Exception {
        UUID responsibleId = createResponsibleUser("Responsible User", "responsible_user@ika.com");

        // Create a new request
        mockMvc.perform(post("/v1/responsibles")
                        .param("idResponsible", responsibleId.toString())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // Accept the request
        mockMvc.perform(put("/v1/responsibles/accept")
                        .param("idResponsible", responsibleId.toString())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    void testDeleteRequestByResponsibleSuccess() throws Exception {
        UUID responsibleId = createResponsibleUser("Responsible User", "responsible_user@ika.com");

        // Create a new request
        mockMvc.perform(post("/v1/responsibles")
                        .param("idResponsible", responsibleId.toString())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // Delete the request by the responsible user
        mockMvc.perform(delete("/v1/responsibles/by-responsible")
                        .param("idResponsible", responsibleId.toString())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAllResponsibleWithFilterAndPagination() throws Exception {
        UUID responsibleId = createResponsibleUser("Responsible User", "responsible_user@ika.com");

        // Create multiple responsible requests
        createResponsibleRequest(responsibleId);
        createResponsibleRequest(responsibleId);

        // Perform the GET request with filter and pagination
        mockMvc.perform(get("/v1/responsibles/responsible")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].responsibleId").value(responsibleId.toString()));
    }

    private UUID createResponsibleUser(String displayName, String email) throws Exception {
        createUser(displayName, email, "password", "pt");
        User user = userRepository.findByEmail(email).orElseThrow();
        return user.getId();
    }

    private void createResponsibleRequest(UUID responsibleId) throws Exception {
        mockMvc.perform(post("/v1/responsibles")
                        .param("idResponsible", responsibleId.toString())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
