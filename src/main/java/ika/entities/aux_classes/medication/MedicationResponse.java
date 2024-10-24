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
        this.maxValidationTime = medication.getMaxValidationTime();
        this.timeBetween = medication.getTimeBetween();
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

    private float maxValidationTime;

    private float timeBetween;
}