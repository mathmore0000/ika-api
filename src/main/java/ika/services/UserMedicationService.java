package ika.services;

import ika.entities.UserMedicationStatus;
import ika.entities.aux_classes.user_medication.UserMedicationRequest;
import ika.entities.Medication;
import ika.entities.User;
import ika.entities.UserMedication;
import ika.repositories.MedicationRepository;
import ika.repositories.UserMedicationRepository;
import ika.repositories.UserMedicationStatusRepository;
import ika.repositories.UserRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserMedicationService {

    @Autowired
    private UserMedicationRepository userMedicationRepository;

    @Autowired
    private UserMedicationStatusRepository userMedicationStatusRepository;

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
        userMedication.setCreatedAt(OffsetDateTime.now());
        // Set defaults if values are not provided by the user
        if (userMedicationDetails.getMaxTakingTime() <= 0) {
            userMedication.setMaxTakingTime(medication.getMaxTakingTime());
        }
        if (userMedicationDetails.getTimeBetween() <= 0) {
            userMedication.setTimeBetween(medication.getTimeBetween());
        }
        if (userMedicationDetails.getQuantityMl() <= 0) {
            userMedication.setQuantityMl(medication.getQuantityMl());
        }
        if (userMedicationDetails.getQuantityInt() <= 0) {
            userMedication.setQuantityInt(medication.getQuantityInt());
        }

        Optional<UserMedication> optionalUserMedication = Optional.of(userMedicationRepository.save(userMedication));

        UserMedicationStatus userMedicationStatus = new UserMedicationStatus();
        userMedicationStatus.setActive(true);
        userMedicationStatus.setUserMedication(optionalUserMedication.get());
        userMedicationStatusRepository.save(userMedicationStatus);

        return optionalUserMedication;
    }

    public Page<UserMedication> getAllUserMedications(UUID userId, Pageable pageable) {
        return userMedicationRepository.findByUserId(userId, pageable);
    }

    // Method to update the 'disabled' status of a user medication
    public UserMedication updateUserMedicationStatus(UUID userId, UUID medicationId, boolean disabled) {
        UserMedication userMedication = userMedicationRepository.findByUserIdAndMedicationId(userId, medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("User medication not found"));
        userMedication.setDisabled(disabled);

        userMedicationRepository.save(userMedication);

        UserMedicationStatus userMedicationStatus = new UserMedicationStatus();
        userMedicationStatus.setActive(!disabled);
        userMedicationStatus.setUserMedication(userMedication);
        userMedicationStatusRepository.save(userMedicationStatus);

        return userMedication;
    }

    // Method to update a user medication with new details
    public UserMedication updateUserMedication(UUID userId, UUID medicationId, UserMedicationRequest request) {
        UserMedication existingUserMedication = userMedicationRepository.findByUserIdAndMedicationId(userId, medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication relation not found"));
        ;
        UserMedication userMedication = existingUserMedication;

        // Update fields
        userMedication.setQuantityInt(request.getQuantityInt());
        userMedication.setQuantityMl(request.getQuantityMl());
        userMedication.setTimeBetween(request.getTimeBetween());
        userMedication.setFirstDosageTime(request.getFirstDosageTime());
        userMedication.setMaxTakingTime(request.getMaxTakingTime());

        userMedicationRepository.save(userMedication);
        return userMedication;
    }
}
