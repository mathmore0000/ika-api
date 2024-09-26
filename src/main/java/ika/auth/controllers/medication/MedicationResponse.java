package ika.auth.controllers.medication;

import ika.auth.entities.ActiveIngredient;
import ika.auth.entities.Category;
import ika.auth.entities.Medication;
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
        this.quantityCard = medication.getQuantityCard();
        this.isValid = medication.isValid();
        this.maxTime = medication.getMaxTime();
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

    private int quantityCard;

    private boolean isValid;

    private float maxTime;

    private float timeBetween;
}