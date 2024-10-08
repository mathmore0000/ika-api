package ika.controllers.aux_classes.category;

import ika.entities.Category;

import java.util.UUID;

public class CategoryResponse {

    private UUID id;
    private String description;

    // Construtor que aceita uma entidade Category
    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.description = category.getDescription();
    }

    // Getters e Setters
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
