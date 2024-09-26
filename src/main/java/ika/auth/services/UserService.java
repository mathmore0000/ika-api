package ika.auth.services;

import ika.auth.entities.User;
import ika.auth.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder; // Use PasswordEncoder genérico

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) { // Aceite o genérico
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        // Hash da senha antes de salvar o usuário
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPasswordHash(hashedPassword);

        return userRepository.save(user);
    }

    // default jwt method
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // Autenticar pelo e-mail
                .password(user.getPasswordHash()) // Usar passwordHash
                .authorities(user.getRole().getRole()) // Pegar o nome do papel (Role)
                .build();
    }

    public Optional<User> loadUserByEmail(String email) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(email);

        return user;
    }

    public Boolean emailExists(String email)  {
        return userRepository.existsByEmail(email);
    }
}