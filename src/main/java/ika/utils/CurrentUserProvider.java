package ika.utils;

import ika.entities.User;
import ika.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserProvider {

    private final UserService userService;
    private User cachedUser;

    public CurrentUserProvider(UserService userService) {
        this.userService = userService;
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

    public void clearCache() {
        cachedUser = null;
    }
}
