package ika.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "category", schema = "public")
public class Category {

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
