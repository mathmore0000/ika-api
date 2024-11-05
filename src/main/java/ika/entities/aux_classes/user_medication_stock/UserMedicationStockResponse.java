package ika.entities.aux_classes.user_medication_stock;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserMedicationStockResponse {

    private UUID id;
    private int quantityStocked;
    private OffsetDateTime stockedAt;
    private OffsetDateTime expirationDate;

    public UserMedicationStockResponse(UUID id, int quantityStocked, OffsetDateTime stockedAt, OffsetDateTime expirationDate) {
        this.id = id;
        this.quantityStocked = quantityStocked;
        this.stockedAt = stockedAt;
        this.expirationDate = expirationDate;
    }
}
