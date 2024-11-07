// LocaleRequest.java
package ika.entities.aux_classes.User;

import jakarta.validation.constraints.NotBlank;
import ika.validators.LocaleConstraint;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocaleRequest {

    @Size(min = 2, max = 2, message = "Invalid locale size")
    @NotBlank(message = "Locale cannot be empty")
    @LocaleConstraint // Custom annotation to validate locale
    private String locale;
}
