package ika.entities.aux_classes.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationTokenChangeRequest {
    @NotBlank(message = "Notification token cannot be empty")
    private String notificationToken;
}
