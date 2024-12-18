package ika.services;

import ika.entities.aux_classes.medication.MedicationRequest;
import ika.entities.aux_classes.medication.MedicationResponse;
import ika.entities.Medication;
import ika.entities.User;
import ika.repositories.MedicationRepository;
import ika.utils.CurrentUserProvider;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@Service
public class MedicationService {

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
        System.out.println("current user " + user);
        Medication medication = new Medication();
        medication.setName(medicationRequest.getName());
        medication.setCategory(categoryService.findById(medicationRequest.getCategoryId()));
        medication.setActiveIngredient(activeIngredientService.findById(medicationRequest.getActiveIngredientId()));
        medication.setDosage(medicationRequest.getDosage());
        medication.setDisabled(false);
        medication.setUser(user);
        medication.setMaxTakingTime(medicationRequest.getMaxTakingTime());
        medication.setTimeBetween(medicationRequest.getTimeBetween());
        medication.setBand(medicationRequest.getBand());
        medication.setQuantityMl(medicationRequest.getQuantityMl());
        medication.setQuantityInt(medicationRequest.getQuantityInt());
        medicationRepository.save(medication);
    }

    @ExceptionHandler(RuntimeException.class)
    public MedicationResponse getMedicationById(UUID id) {
        System.out.println("id" + id);
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found"));
        System.out.println("medication -> " + medication);
        return new MedicationResponse(medication);
    }

    public Page<MedicationResponse> getAllMedications(String name, UUID categoryId, UUID activeIngredientId, Pageable pageable) {
        System.out.println(name + categoryId + activeIngredientId + pageable.toString());
        UUID userId = currentUserProvider.getCurrentUserId();
        return medicationRepository.findAllWithFilters(name, categoryId, activeIngredientId, userId, pageable)
                .map(this::convertToMedicationResponse);
    }
    private MedicationResponse convertToMedicationResponse(Medication medication) {
        return new MedicationResponse(medication);
    }
}
