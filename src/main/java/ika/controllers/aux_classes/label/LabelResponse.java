package ika.controllers.aux_classes.label;

import java.util.UUID;

public class LabelResponse {

    private UUID id;
    private String description;

    public LabelResponse(UUID id, String description) {
        this.id = id;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
