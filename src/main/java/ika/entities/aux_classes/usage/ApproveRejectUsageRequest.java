package ika.entities.aux_classes.usage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class ApproveRejectUsageRequest {
    // Getters and Setters
    private String obs;

    @Size(min = 1, message="At least 1 label is required")
    @NotNull(message="Labels are required")
    private List<UUID> labels;

}
