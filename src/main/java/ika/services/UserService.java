package ika.services;

import ika.entities.FileEntity;
import ika.entities.User;
import ika.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder; // Use PasswordEncoder genérico
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private FileService fileService;

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

    public String changeImage(User user, MultipartFile image) throws Exception {
        // Upload new image
        FileEntity newFile = fileService.uploadImage(user.getId(), "avatar-images", image);

        String newUrl = fileService.getPublicUrl(newFile);
        String oldUrl = user.getAvatarUrl();

        // Update user with new URL
        user.setAvatarUrl(newUrl);
        userRepository.save(user);

        // Delete old image if exists
        if (oldUrl != null && !oldUrl.isEmpty()) {
            UUID fileId = fileService.getFileIdFromUrl(oldUrl);
            System.out.println("fileId a ser removido " + fileId);
            fileService.deleteFile(fileId);
        }

        return newUrl;
    }

    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Old password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        updateUser(user);
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

    public Boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void updateUser(User user) {
        try {
            userRepository.save(user);
        } catch (Exception e) {
            // Log the exception
            throw new RuntimeException("Error updating user", e);
        }
    }
}