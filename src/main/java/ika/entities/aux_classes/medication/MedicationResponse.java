package ika.entities.aux_classes.medication;

import ika.entities.ActiveIngredient;
import ika.entities.Category;
import ika.entities.Medication;
import lombok.Data;

import java.util.UUID;

@Data
public class MedicationResponse {

    public MedicationResponse(Medication medication){
        this.id = medication.getId();
        this.name = medication.getName();
        this.disabled = medication.isDisabled();
        this.band = medication.getBand();
        this.rating = medication.getRating();
        this.activeIngredient = medication.getActiveIngredient();
        this.category = medication.getCategory();
        this.dosage = medication.getDosage();
        this.isValid = medication.isValid();
        this.maxTakingTime = medication.getMaxTakingTime();
        this.timeBetween = medication.getTimeBetween();
        this.quantityInt = medication.getQuantityInt();
        this.quantityMl = medication.getQuantityMl();
    }
    private UUID id;

    private String name;

    private boolean disabled;

    private int band;

    private float rating;

    private ActiveIngredient activeIngredient;

    private Category category;

    private float dosage;

    private boolean isValid;

    private float maxTakingTime;

    private float timeBetween;

    private Float quantityMl;

    private Integer quantityInt;
}