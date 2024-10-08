//package ika;
//
//import ika.auth.controllers.aux_classes.auth.AuthRequest;
//import ika.auth.controllers.aux_classes.auth.SignUpRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import org.springframework.test.web.servlet.MockMvc;
//
//public class Util {
//    @Autowired
//    private MockMvc mockMvc;
//
//    public void createUser(String displayName, String email, String password, String locale) throws Exception {
//        SignUpRequest signUpRequest = new SignUpRequest();
//        signUpRequest.setDisplayName(displayName);
//        signUpRequest.setEmail(email);
//        signUpRequest.setPassword(password);
//        signUpRequest.setLocale(locale);
//
//        mockMvc.perform(post("/v1/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(signUpRequest)))
//                .andExpect(status().isOk())
//                .andExpect(content().string("User registered successfully"));
//    }
//
//    public void createUserAndSetToken() throws Exception {
//        // Cria um usuário para obter o token
//        createUser("Default User", "default@example.com", "password", "en");
//
//        // Executa o login para obter o JWT
//        AuthRequest authRequest = new AuthRequest();
//        authRequest.setUsername("default@example.com");
//        authRequest.setPassword("password");
//
//        String response = mockMvc.perform(post("/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(authRequest)))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
//        jwtToken = tokenResponse.getJwt();
//    }
//}
