package ika.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

// Bucket Entity
@Entity
@Data
@Table(name = "buckets", schema = "storage")
public class Bucket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String description;

    public String getName(){
        return this.description + "-" + this.id.toString();
    }
}
