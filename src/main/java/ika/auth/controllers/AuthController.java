package ika.auth.controllers;

import ika.auth.controllers.aux_classes.auth.AuthRequest;
import ika.auth.controllers.aux_classes.auth.RefreshTokenRequest;
import ika.auth.controllers.aux_classes.auth.SignUpRequest;
import ika.auth.controllers.aux_classes.auth.TokenResponse;
import ika.auth.entities.Role;
import ika.auth.entities.User;
import ika.auth.services.RoleService;
import ika.auth.services.UserService;
import ika.auth.utils.JwtUtil;
import jakarta.validation.Valid;
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
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userService.loadUserByUsername(username);

        String newJwt = jwtUtil.generateToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        TokenResponse tokenResponse = new TokenResponse(newJwt, newRefreshToken);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        final UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        TokenResponse tokenResponse = new TokenResponse(jwt, refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        // Verifique se o e-mail ou telefone já existe no banco de dados
        if (userService.emailExists(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        Role defaultRole = roleService.findRoleByName(Role.USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Crie e salve o novo usuário
        User newUser = User.builder()
                .displayName(signUpRequest.getDisplayName())
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
                .locale(signUpRequest.getLocale())
                .role(defaultRole)
                .metadata("{}")
                .createdAt(LocalDateTime.now())
                .build();

        userService.createUser(newUser);

        return ResponseEntity.ok("User registered successfully");
    }
}
