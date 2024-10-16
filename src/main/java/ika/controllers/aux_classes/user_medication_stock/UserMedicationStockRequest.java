package ika.controllers.aux_classes.user_medication_stock;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserMedicationStockRequest {

    @NotNull
    private UUID userMedicationId;

    @NotNull
    private int quantityStocked;

    @NotNull
    private LocalDateTime expirationDate;

    private Integer quantityCard;
}
