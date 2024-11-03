package ika.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "medication", schema = "public")
public class Medication {

    public Medication(UUID id, String name, boolean disabled, int band, Float rating,
                      ActiveIngredient activeIngredient, Category category, float dosage,
                      User user, boolean isValid, float maxTakingTime, float timeBetween, Integer quantityInt, Float quantityMl) {
        this.id = id;
        this.name = name;
        this.disabled = disabled;
        this.band = band;
        this.rating = rating;
        this.activeIngredient = activeIngredient;
        this.category = category;
        this.dosage = dosage;
        this.user = user;
        this.isValid = isValid;
        this.maxTakingTime = maxTakingTime;
        this.timeBetween = timeBetween;
        this.quantityInt = quantityInt;
        this.quantityMl = quantityMl;
    }

    public Medication() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "disabled", nullable = false)
    private boolean disabled;

    @Column(name = "band", nullable = false)
    private int band;

    @Column(name = "rating")
    private float rating;

    @ManyToOne
    @JoinColumn(name = "id_active_ingredient", nullable = false)
    private ActiveIngredient activeIngredient;

    @ManyToOne
    @JoinColumn(name = "id_category", nullable = false)
    private Category category;

    @Column(name = "dosage", nullable = false)
    private float dosage;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @Column(name = "is_valid", nullable = false)
    private boolean isValid;

    @Column(name = "max_taking_time", nullable = false)
    private float maxTakingTime;

    @Column(name = "quantity_int")
    private Integer quantityInt;

    @Column(name = "quantity_ml")
    private Float quantityMl;

    @Column(name = "time_between", nullable = false)
    private float timeBetween;
}
