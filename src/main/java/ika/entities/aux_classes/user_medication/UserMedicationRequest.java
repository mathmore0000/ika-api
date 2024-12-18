package ika.entities.aux_classes.user_medication;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserMedicationRequest {

    @NotNull(message = "idMedication is required")
    private UUID idMedication;

    private Integer quantityInt = 0;;

    private Float quantityMl = 0F;

    private float timeBetween;

    @NotNull(message = "firstDosageTime is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private OffsetDateTime firstDosageTime;

    private float maxTakingTime;
}
