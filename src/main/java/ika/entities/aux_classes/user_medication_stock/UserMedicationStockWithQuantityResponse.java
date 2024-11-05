package ika.entities.aux_classes.user_medication_stock;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserMedicationStockWithQuantityResponse {

    private UUID id;
    private int quantityStocked;
    private Number availableQuantity;
    private OffsetDateTime stockedAt;
    private OffsetDateTime expirationDate;

    public UserMedicationStockWithQuantityResponse(UUID id, Number availableQuantity, int quantityStocked, OffsetDateTime stockedAt, OffsetDateTime expirationDate) {
        this.id = id;
        this.quantityStocked = quantityStocked;
        this.availableQuantity = availableQuantity;
        this.stockedAt = stockedAt;
        this.expirationDate = expirationDate;
    }
}
