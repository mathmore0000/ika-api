// DateOfBirthRequest.java
package ika.entities.aux_classes.User;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DateOfBirthRequest {

    @NotNull(message = "Date of birth cannot be empty")
    private Date dateOfBirth;
}
