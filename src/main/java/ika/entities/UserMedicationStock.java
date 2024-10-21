package ika.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_medication_stock", schema = "public")
public class UserMedicationStock {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_user_medication", nullable = false)
    private UserMedication userMedication;

    @Column(name = "quantity_stocked", nullable = false)
    private int quantityStocked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "stocked_at", nullable = false)
    private LocalDateTime stockedAt;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @OneToMany(mappedBy = "userMedicationStock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMedicationStockUsage> stockUsages;
}
