package ika.utils;

import ika.entities.User;
import ika.services.UserService;
import ika.utils.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CurrentUserProvider {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private User cachedUser;

    public CurrentUserProvider(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    public User getCurrentUser() {
        if (cachedUser == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                String email = ((UserDetails) authentication.getPrincipal()).getUsername();
                // Assumindo que você tem um método para buscar o usuário pelo e-mail

                Optional<User> user = userService.loadUserByEmail(email);
                if (user.isEmpty()){
                    throw new UsernameNotFoundException("User not found with email: " + email);
                }
                cachedUser = user.get();
            }
        }
        return cachedUser;
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            String token = (String) authentication.getCredentials(); // Assume JWT is passed as credentials
            return jwtUtil.extractUserId(token); // Extract user ID from the token
        }
        throw new RuntimeException("No authentication token found");
    }

    public void clearCache() {
        cachedUser = null;
    }
}
