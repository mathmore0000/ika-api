package ika.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "active_ingredient", schema = "public")
public class ActiveIngredient {

    public ActiveIngredient(UUID id, String description) {
        this.id = id;
        this.description = description;
    }

    public ActiveIngredient() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;


    @Column(name = "description", nullable = false)
    private String description;

    // Getters e Setters
    public UUID getId() {
        return id;
    }
    public String getDescription() {
        return description;
    }
    public String setDescription(String description) {
        return this.description = description;
    }
}
