package ika.controllers.aux_classes.active_ingredient;

import ika.entities.ActiveIngredient;

import java.util.UUID;

public class ActiveIngredientResponse {

    private UUID id;
    private String description;

    public ActiveIngredientResponse(ActiveIngredient ingredient) {
        this.id = ingredient.getId();
        this.description = ingredient.getDescription();
    }

    // Getter e Setter
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
