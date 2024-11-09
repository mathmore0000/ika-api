// UserController.java
package ika.controllers;

import ika.entities.User;
import ika.entities.aux_classes.User.*;
import ika.services.UserService;
import ika.utils.CurrentUserProvider;
import ika.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/v1/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @PatchMapping("/locale")
    public ResponseEntity<String> updateLocale(@RequestBody @Valid LocaleRequest localeRequest) {
        // Get the authenticated user's email from the security context
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Load the user by email
        Optional<User> optionalUser = userService.loadUserByEmail(userEmail);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        User user = optionalUser.get();

        // Update the user's locale
        user.setLocale(localeRequest.getLocale());

        // Save the updated user
        userService.updateUser(user);

        return ResponseEntity.ok("Locale updated successfully");
    }

    // Endpoint para atualizar o displayName
    @PatchMapping("/display-name")
    public ResponseEntity<String> updateDisplayName(@RequestBody @Valid DisplayNameRequest displayNameRequest) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userService.loadUserByEmail(userEmail);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        User user = optionalUser.get();
        user.setDisplayName(displayNameRequest.getDisplayName());
        userService.updateUser(user);

        return ResponseEntity.ok("Display name updated successfully");
    }

    // Endpoint para atualizar o phoneNumber
    @PatchMapping("/phone-number")
    public ResponseEntity<String> updatePhoneNumber(@RequestBody @Valid PhoneNumberRequest phoneNumberRequest) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userService.loadUserByEmail(userEmail);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        User user = optionalUser.get();
        user.setPhoneNumber(phoneNumberRequest.getPhoneNumber());
        userService.updateUser(user);

        return ResponseEntity.ok("Phone number updated successfully");
    }

    // Endpoint para atualizar a dateOfBirth
    @PatchMapping("/date-of-birth")
    public ResponseEntity<String> updateDateOfBirth(@RequestBody @Valid DateOfBirthRequest dateOfBirthRequest) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userService.loadUserByEmail(userEmail);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        User user = optionalUser.get();
        user.setBirthDate(dateOfBirthRequest.getDateOfBirth());
        userService.updateUser(user);

        return ResponseEntity.ok("Date of birth updated successfully");
    }

    // Endpoint para trocar a senha
    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest) {
        User user = currentUserProvider.getCurrentUser();
        userService.changePassword(user, passwordChangeRequest.getOldPassword(), passwordChangeRequest.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");

    }

    // Endpoint para trocar a imagem de pefil
    @PatchMapping("/change-image")
    public ResponseEntity<String> changeImage(@RequestBody @RequestParam("image") MultipartFile image) throws Exception {
        User user = currentUserProvider.getCurrentUser();
        String publicUrl = userService.changeImage(user, image);
        return ResponseEntity.ok(publicUrl);
    }

    // Endpoint para trocar token de notificação
    @PatchMapping("/change-notification-token")
    public ResponseEntity<String> changeNotificationToken(@RequestBody @Valid NotificationTokenChangeRequest notificationTokenChangeRequest) {
        User user = currentUserProvider.getCurrentUser();
        userService.changeNotificationToken(user, notificationTokenChangeRequest.getNotificationToken());
        return ResponseEntity.ok("Notification token updated");
    }
}
