package ika.auth.services;

import ika.auth.controllers.medication.MedicationRequest;
import ika.auth.controllers.medication.MedicationResponse;
import ika.auth.entities.Medication;
import ika.auth.entities.User;
import ika.auth.repositories.MedicationRepository;
import ika.auth.utils.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MedicationService {

    private UserService userService;
    private final CurrentUserProvider currentUserProvider;
    @Autowired
    private final MedicationRepository medicationRepository;

    @Autowired
    private final CategoryService categoryService;

    @Autowired
    private final ActiveIngredientService activeIngredientService;

    @Autowired
    public MedicationService(MedicationRepository medicationRepository,
                             CategoryService categoryService,
                             ActiveIngredientService activeIngredientService,
                             CurrentUserProvider currentUserProvider) {
        this.medicationRepository = medicationRepository;
        this.categoryService = categoryService;
        this.activeIngredientService = activeIngredientService;
        this.currentUserProvider = currentUserProvider;
    }

    public void createMedication(MedicationRequest medicationRequest) {
        System.out.println("medication request " + medicationRequest);
        User user = currentUserProvider.getCurrentUser();
        Medication medication = new Medication();
        medication.setName(medicationRequest.getName());
        medication.setCategory(categoryService.findById(medicationRequest.getCategoryId()));
        medication.setActiveIngredient(activeIngredientService.findById(medicationRequest.getActiveIngredientId()));
        medication.setDosage(medicationRequest.getDosage());
        medication.setQuantityCard(medicationRequest.getQuantityCard());
        medication.setDisabled(false);
        medication.setUser(user);
        medication.setMaxTime(medicationRequest.getMaxTime());
        medication.setTimeBetween(medicationRequest.getTimeBetween());
        medication.setBand(medicationRequest.getBand());
        medicationRepository.save(medication);
    }

    public MedicationResponse getMedicationById(UUID id) {
        System.out.println("id" + id);
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        System.out.println("medication -> " + medication);
        return new MedicationResponse(medication);
    }
}
