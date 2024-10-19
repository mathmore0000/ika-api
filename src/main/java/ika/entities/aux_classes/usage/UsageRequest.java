package ika.entities.aux_classes.usage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UsageRequest {

    @NotNull(message = "Action timestamp is required")
    private LocalDateTime actionTmstamp;

    @NotNull(message = "Medications are required")
    @Valid  // Garantir que os elementos dentro da lista também sejam validados
    private List<MedicationStockRequest> medications;

    @Data
    public static class MedicationStockRequest {

        @NotNull(message = "MedicationStockId is required")
        private UUID medicationStockId;  // ID do estoque de medicação

        private Integer quantityInt;

        private Float quantityMl;
    }
}
