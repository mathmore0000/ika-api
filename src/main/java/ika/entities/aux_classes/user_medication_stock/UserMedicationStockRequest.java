package ika.entities.aux_classes.user_medication_stock;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserMedicationStockRequest {

    @NotNull(message = "userMedicationId is required")
    private UUID userMedicationId;

    @NotNull(message = "quantityStocked is required")
    private Integer quantityStocked;

    @NotNull(message = "expirationDate is required")
    private LocalDateTime expirationDate;

    private Integer quantityCard;
}
