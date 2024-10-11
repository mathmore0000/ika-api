package ika.services;

import ika.controllers.aux_classes.user_medication.UserMedicationRequest;
import ika.entities.Medication;
import ika.entities.User;
import ika.entities.UserMedication;
import ika.repositories.MedicationRepository;
import ika.repositories.UserMedicationRepository;
import ika.repositories.UserRepository;
import ika.repositories.UserResponsibleRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserMedicationService {

    @Autowired
    private UserMedicationRepository userMedicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    public Optional<UserMedication> addUserMedication(UUID userId, UUID medicationId, UserMedicationRequest userMedicationDetails) {
        // Retrieve user and medication entities to establish relationship
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found"));

        if (userMedicationRepository.existsByUserIdAndMedicationId(userId, medicationId)) {
            return Optional.empty();
        }

        UserMedication userMedication = new UserMedication(userMedicationDetails);
        userMedication.setUser(user);
        userMedication.setMedication(medication);
        // Set defaults if values are not provided by the user
        if (userMedicationDetails.getMaxValidationTime() <= 0) {
            userMedication.setMaxValidationTime(medication.getMaxValidationTime());
        }
        if (userMedicationDetails.getTimeBetween() <= 0) {
            userMedication.setTimeBetween(medication.getTimeBetween());
        }
        if (userMedicationDetails.getQuantityCard() <= 0) {
            userMedication.setQuantityCard(medication.getQuantityCard());
        }
        if (userMedicationDetails.getQuantityMl() <= 0) {
            userMedication.setQuantityMl(medication.getQuantityMl());
        }
        if (userMedicationDetails.getQuantityInt() <= 0) {
            userMedication.setQuantityInt(medication.getQuantityInt());
        }

        return Optional.of(userMedicationRepository.save(userMedication));
    }

    public Page<UserMedication> getAllUserMedications(UUID userId, Pageable pageable) {
        return userMedicationRepository.findByUserId(userId, pageable);
    }

    // Method to update the 'disabled' status of a user medication
    public UserMedication updateUserMedicationStatus(UUID userId, UUID userMedicationId, boolean disabled) {
        UserMedication userMedication = userMedicationRepository.findByUserIdAndMedicationId(userId, userMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("User medication not found"));
        userMedication.setDisabled(disabled);
        userMedicationRepository.save(userMedication);
        return userMedication;
    }

    // Method to update a user medication with new details
    public UserMedication updateUserMedication(UUID userId, UUID userMedicationId, UserMedicationRequest request) {
        UserMedication existingUserMedication = userMedicationRepository.findByUserIdAndMedicationId(userId, userMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication relation not found"));;
            UserMedication userMedication = existingUserMedication;

            // Update fields
            userMedication.setQuantityInt(request.getQuantityInt());
            userMedication.setQuantityMl(request.getQuantityMl());
            userMedication.setTimeBetween(request.getTimeBetween());
            userMedication.setFirstDosageTime(request.getFirstDosageTime());
            userMedication.setMaxValidationTime(request.getMaxValidationTime());
            userMedication.setQuantityCard(request.getQuantityCard());

            userMedicationRepository.save(userMedication);
            return userMedication;
    }

    public void deleteUserMedication(UUID userId, UUID medicationId) {
        Optional<UserMedication> userMedication = userMedicationRepository.findByUserIdAndMedicationId(userId, medicationId);
        userMedication.ifPresent(userMedicationRepository::delete);
    }
}
