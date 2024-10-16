package ika.repositories;

import ika.entities.UserMedicationStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserMedicationStockRepository extends JpaRepository<UserMedicationStock, UUID> {
    @Query("SELECT ums FROM UserMedicationStock ums WHERE ums.userMedication.user.id = :userId AND ums.userMedication.medication.id = :medicationId")
    Page<UserMedicationStock> findAllByUserIdAndMedicationId(UUID userId, UUID medicationId, Pageable pageable);
}
