package ika.auth.controllers;

import ika.auth.entities.Role;
import ika.auth.repositories.RoleRepository;
import ika.auth.services.RoleService;
import ika.auth.services.UserService;
import ika.auth.utils.JwtUtil;
import ika.auth.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) throws Exception {
        System.out.println("Login request received");
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            System.out.println("Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        final UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest signUpRequest) {
        // Verifique se o e-mail ou telefone já existe no banco de dados
//        if (userService.emailExists(signUpRequest.getEmail())) {
//            return ResponseEntity.badRequest().body("Email already in use");
//        }
//        if (userService.phoneNumberExists(signUpRequest.getPhoneNumber())) {
//            return ResponseEntity.badRequest().body("Phone number already in use");
//        }

        Role userRole = roleService.findRoleByName("USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Crie e salve o novo usuário
        User newUser = User.builder()
                .displayName(signUpRequest.getDisplayName())
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword()) // Note que estamos salvando o hash da senha
                .locale(signUpRequest.getLocale())
                .disabled(false)
                .role(userRole)
                .createdAt(LocalDateTime.now())
                .build();

        userService.createUser(newUser);

        return ResponseEntity.ok("User registered successfully");
    }
}
