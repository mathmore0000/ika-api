package ika.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_medication_status", schema = "public")
public class UserMedicationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_user_medication", nullable = false)
    @JsonBackReference
    private UserMedication userMedication;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // Default constructor
    public UserMedicationStatus() {}

    // Constructor with fields
    public UserMedicationStatus(UserMedication userMedication, boolean active) {
        this.userMedication = userMedication;
        this.active = active;
        this.createdAt = OffsetDateTime.now();
    }
}
