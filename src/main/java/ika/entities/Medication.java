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
                      Integer quantityCard, User user, boolean isValid, float maxTime, float timeBetween) {
        this.id = id;
        this.name = name;
        this.disabled = disabled;
        this.band = band;
        this.rating = rating;
        this.activeIngredient = activeIngredient;
        this.category = category;
        this.dosage = dosage;
        this.quantityCard = quantityCard;
        this.user = user;
        this.isValid = isValid;
        this.maxTime = maxTime;
        this.timeBetween = timeBetween;
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

    @Column(name = "quantity_card")
    private int quantityCard;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @Column(name = "is_valid", nullable = false)
    private boolean isValid;

    @Column(name = "max_time", nullable = false)
    private float maxTime;

    @Column(name = "time_between", nullable = false)
    private float timeBetween;
}
