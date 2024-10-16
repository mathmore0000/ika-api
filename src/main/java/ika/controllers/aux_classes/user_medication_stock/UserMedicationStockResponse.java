package ika.controllers.aux_classes.user_medication_stock;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserMedicationStockResponse {

    private UUID id;
    private int quantityStocked;
    private int quantityCard;
    private LocalDateTime stockedAt;
    private LocalDateTime expirationDate;

    public UserMedicationStockResponse(UUID id, int quantityStocked, int quantityCard, LocalDateTime stockedAt, LocalDateTime expirationDate) {
        this.id = id;
        this.quantityStocked = quantityStocked;
        this.quantityCard = quantityCard;
        this.stockedAt = stockedAt;
        this.expirationDate = expirationDate;
    }
}
