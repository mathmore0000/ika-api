package ika.entities;

import ika.entities.aux_classes.user_medication.UserMedicationRequest;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_medication", schema = "public")
public class UserMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_medication", nullable = false)
    private Medication medication;

    @Column(name = "disabled", nullable = false)
    private boolean disabled = false;

    @Column(name = "quantity_int")
    private Integer quantityInt;

    @Column(name = "quantity_ml")
    private Float quantityMl;

    @Column(name = "time_between", nullable = false)
    private float timeBetween;

    @Column(name = "first_dosage_time", nullable = false)
    private OffsetDateTime firstDosageTime;

    @Column(name = "max_taking_time", nullable = false)
    private float maxTakingTime;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "userMedication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserMedicationStatus> userMedicationStatuses;

    // Default constructor
    public UserMedication() {
    }

    // Constructor that sets defaults based on the medication values
    public UserMedication(User user, Medication medication, Integer quantityInt, Float quantityMl) {
        this.user = user;
        this.medication = medication;
        this.timeBetween = medication.getTimeBetween();
        this.maxTakingTime = medication.getMaxTakingTime();
        this.quantityInt = quantityInt;
        this.quantityMl = quantityMl;
    }

    public UserMedication(UserMedicationRequest userMedicationDetails) {
        this.maxTakingTime = userMedicationDetails.getMaxTakingTime();
        this.disabled = false;
        this.quantityInt = userMedicationDetails.getQuantityInt();
        this.quantityMl = userMedicationDetails.getQuantityMl();
        this.timeBetween = userMedicationDetails.getTimeBetween();
        this.firstDosageTime = userMedicationDetails.getFirstDosageTime();
    }
}
