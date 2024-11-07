// DisplayNameRequest.java
package ika.entities.aux_classes.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisplayNameRequest {

    @NotBlank(message = "Display name cannot be empty")
    @Size(min = 2, max = 50, message = "Display name must be between 2 and 50 characters")
    private String displayName;
}
