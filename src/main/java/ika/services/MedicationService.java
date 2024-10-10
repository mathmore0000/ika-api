package ika.services;

import ika.controllers.aux_classes.medication.MedicationRequest;
import ika.controllers.aux_classes.medication.MedicationResponse;
import ika.entities.Medication;
import ika.entities.User;
import ika.repositories.MedicationRepository;
import ika.utils.CurrentUserProvider;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
        medication.setMaxValidationTime(medicationRequest.getMaxValidationTime());
        medication.setTimeBetween(medicationRequest.getTimeBetween());
        medication.setBand(medicationRequest.getBand());
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
        return medicationRepository.findAllWithFilters(name, categoryId, activeIngredientId, pageable)
                .map(this::convertToMedicationResponse);
    }
    private MedicationResponse convertToMedicationResponse(Medication medication) {
        return new MedicationResponse(medication);
    }
}
