package ika.repositories;

import ika.entities.Usage;
import ika.entities.UserMedication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMedicationRepository extends JpaRepository<UserMedication, UUID> {
    List<UserMedication> findByUserIdAndFirstDosageTimeBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);

    Page<UserMedication> findByUserId(UUID userId, Pageable pageable);

    Optional<UserMedication> findByUserIdAndMedicationId(UUID userId, UUID medicationId);

    Boolean existsByUserIdAndMedicationId(UUID userId, UUID medicationId);

}
