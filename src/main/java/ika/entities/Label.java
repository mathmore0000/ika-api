package ika.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "labels")
public class Label {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String description;

    // Constructors
    public Label() {}

    public Label(UUID id, String description) {
        this.id = id; this.description = description;
    }

    // Getters and Setters
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
