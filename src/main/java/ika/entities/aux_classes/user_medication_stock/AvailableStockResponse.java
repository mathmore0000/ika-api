package ika.entities.aux_classes.user_medication_stock;

import lombok.Data;

@Data
public class AvailableStockResponse {
    private String medicationType; // "solid" ou "liquid"
    private Number availableQuantity;

    // Construtores, getters e setters
    public AvailableStockResponse(String medicationType, Number availableQuantity) {
        this.medicationType = medicationType;
        this.availableQuantity = availableQuantity;
    }

    // Getters e Setters
}

