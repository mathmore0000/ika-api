package ika.controllers.aux_classes.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignUpRequest {

    @NotNull(message = "Display name is required")
    private String displayName;

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Password is required")
    private String password;

    @NotNull(message = "Locale is required")
    @Size(min = 2, max = 2, message = "Invalid locale length")
    private String locale;
}
