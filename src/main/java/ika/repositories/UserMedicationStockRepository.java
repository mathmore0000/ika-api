package ika.repositories;

import ika.entities.UserMedicationStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserMedicationStockRepository extends JpaRepository<UserMedicationStock, UUID> {
    @Query("SELECT ums FROM UserMedicationStock ums WHERE ums.userMedication.user.id = :userId AND ums.userMedication.medication.id = :medicationId")
    Page<UserMedicationStock> findAllByUserIdAndMedicationId(UUID userId, UUID medicationId, Pageable pageable);

    @Query("SELECT ums FROM UserMedicationStock ums WHERE ums.userMedication.user.id = :userId AND ums.id IN :medicationIds")
    List<UserMedicationStock> findAllByUserIdAndMedicationIds(UUID userId, List<UUID> medicationIds);

    @Query("SELECT ums FROM UserMedicationStock ums WHERE ums.userMedication.user.id = :userId AND ums.userMedication.medication.id = :medicationId AND ums.expirationDate >= :currentDate")
    List<UserMedicationStock> findAllByUserIdAndMedicationIdAndNotExpired(@Param("userId") UUID userId, @Param("medicationId") UUID medicationId, @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT ums FROM UserMedicationStock ums WHERE ums.userMedication.user.id = :userId AND ums.userMedication.medication.id = :medicationId AND ums.expirationDate >= :currentDate order by ums.expirationDate")
    List<UserMedicationStock> findAllByUserIdAndMedicationIdAndNotExpiredOrderedByExpirationDate(@Param("userId") UUID userId, @Param("medicationId") UUID medicationId, @Param("currentDate") LocalDateTime currentDate);
}
