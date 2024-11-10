package ika.services;

import ika.entities.UserMedication;
import ika.entities.UserMedicationStatus;
import ika.repositories.UserMedicationStatusRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserMedicationStatusService {

    private final UserMedicationStatusRepository userMedicationStatusRepository;

    @Autowired
    public UserMedicationStatusService(UserMedicationStatusRepository userMedicationStatusRepository) {
        this.userMedicationStatusRepository = userMedicationStatusRepository;
    }

    // Criar um novo status
    public UserMedicationStatus createStatus(UserMedication userMedication, boolean active) {
        UserMedicationStatus status = new UserMedicationStatus(userMedication, active);
        return userMedicationStatusRepository.save(status);
    }
}
