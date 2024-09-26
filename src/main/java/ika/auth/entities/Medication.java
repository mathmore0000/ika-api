package ika.auth.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "medication", schema = "public")
public class Medication {

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
