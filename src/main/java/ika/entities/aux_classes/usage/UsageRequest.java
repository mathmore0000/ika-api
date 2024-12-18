package ika.entities.aux_classes.usage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UsageRequest {

    @NotNull(message = "Action timestamp is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private OffsetDateTime actionTmstamp;

    @NotNull(message = "Medications are required")
    @Size(max = 30, message = "A maximum of 30 medications are allowed at once")
    @Valid  // Garantir que os elementos dentro da lista também sejam validados
    private List<MedicationStockRequest> medications;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MedicationStockRequest {

        @NotNull(message = "MedicationStockId is required")
        private UUID medicationStockId;  // ID do estoque de medicação

        private Integer quantityInt;

        private Float quantityMl;
    }
}
