package ika.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name = "labels")
public class Label {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String description;

    @ManyToMany(mappedBy = "labels")
    private Set<Usage> usages = new HashSet<>();

    // Constructors
    public Label() {}

    public Label(UUID id, String description) {
        this.id = id; this.description = description;
    }
}
