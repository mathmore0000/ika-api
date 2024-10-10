package ika.repositories;

import ika.entities.UserMedication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserMedicationRepository extends JpaRepository<UserMedication, UUID> {
    Page<UserMedication> findByUserId(UUID userId, Pageable pageable);
    Optional<UserMedication> findByUserIdAndMedicationId(UUID userId, UUID medicationId);
    Boolean existsByUserIdAndMedicationId(UUID userId, UUID medicationId);

}
