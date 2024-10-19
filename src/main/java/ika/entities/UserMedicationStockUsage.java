package ika.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class UserMedicationStockUsage {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_usage")
    private Usage usage;

    @ManyToOne
    @JoinColumn(name = "id_user_medication_stock")
    private UserMedicationStock userMedicationStock;

    private Integer quantityInt;
    private Float quantityMl;
}
