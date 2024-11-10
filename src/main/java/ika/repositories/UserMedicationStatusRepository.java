package ika.repositories;

import ika.entities.UserMedication;
import ika.entities.UserMedicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMedicationStatusRepository extends JpaRepository<UserMedicationStatus, UUID> {
}
