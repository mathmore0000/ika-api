package ika.auth.controllers.medication;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MedicationRequest {
    @NotNull(message = "name is required")
    private String name;

    @NotNull(message = "categoryId is required")
    private UUID categoryId;

    @NotNull(message = "dosage is required")
    private float dosage;

    private int quantityCard;

    @NotNull(message = "activeIngredientId is required")
    private UUID activeIngredientId;

    @Min(value = 1, message = "maxTime must be greater than or equal to 1")
    private float maxTime;

    @Min(value = 1, message = "timeBetween must be greater than or equal to 1")
    private float timeBetween;

    @Min(value = 1, message = "band must be greater than or equal to 1")
    private int band;
}
