package ika.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import ika.controllers.aux_classes.auth.AuthRequest;
import ika.controllers.aux_classes.auth.RefreshTokenRequest;
import ika.controllers.aux_classes.auth.SignUpRequest;
import ika.controllers.aux_classes.auth.TokenResponse;
import ika.repositories.RoleRepository;
import ika.repositories.UserRepository;
//import ika.Util.createUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usar perfil de teste
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    void createUser(String displayName, String email, String password, String locale) throws Exception {
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

    // Definir o contêiner PostgreSQL como estático e anotá-lo com @Container para Testcontainers gerenciar
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.1")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // Configurar propriedades dinâmicas para o Spring usar o PostgreSQL do Testcontainers
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

    @Test
    void testSignUpSuccess() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setDisplayName("Test User");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setLocale("en");

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void testSignUpEmailAlreadyExists() throws Exception {
        // Primeiro, criar um usuário com o email
        createUser("Already Existing User", "test_already_exists@example.com", "newpassword", "en");

        // Tentar registrar novamente com o mesmo email
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setDisplayName("Already Existing User");
        signUpRequest.setEmail("test_already_exists@example.com");
        signUpRequest.setPassword("newpassword");
        signUpRequest.setLocale("en");

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already in use"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Primeiro, criar um usuário
        createUser("Already Login Success User", "test_login_success@example.com", "password", "en");

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("test_login_success@example.com");
        authRequest.setPassword("password");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void testLoginBadCredentials() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("nonexistentuser");
        authRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        // Primeiro, criar um usuário e obter tokens
        createUser("Success Refresh Token User", "test_successful_refresh_token@example.com", "password", "en");

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("test_successful_refresh_token@example.com");
        authRequest.setPassword("password");

        String response = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);

        // Agora, usar o refresh token para obter novos tokens
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken(tokenResponse.getRefreshToken());

        mockMvc.perform(post("/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void testRefreshTokenInvalid() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("invalidToken");

        mockMvc.perform(post("/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isUnauthorized());
    }
}
