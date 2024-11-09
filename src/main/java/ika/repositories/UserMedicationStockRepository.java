package ika.repositories;

import ika.entities.UserMedicationStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface UserMedicationStockRepository extends JpaRepository<UserMedicationStock, UUID> {
    @Query("SELECT ums FROM UserMedicationStock ums WHERE ums.userMedication.user.id = :userId AND ums.userMedication.medication.id = :medicationId")
    Page<UserMedicationStock> findAllByUserIdAndMedicationId(UUID userId, UUID medicationId, Pageable pageable);

    @Query("SELECT ums FROM UserMedicationStock ums WHERE ums.userMedication.user.id = :userId AND ums.id IN :medicationIds")
    List<UserMedicationStock> findAllByUserIdAndMedicationIds(UUID userId, List<UUID> medicationIds);

    @Query("SELECT ums FROM UserMedicationStock ums WHERE ums.userMedication.user.id = :userId AND ums.userMedication.medication.id = :medicationId AND ums.expirationDate >= :currentDate")
    List<UserMedicationStock> findAllByUserIdAndMedicationIdAndNotExpired(@Param("userId") UUID userId, @Param("medicationId") UUID medicationId, @Param("currentDate") OffsetDateTime currentDate);

    @Query("SELECT ums FROM UserMedicationStock ums WHERE ums.userMedication.user.id = :userId AND ums.userMedication.medication.id = :medicationId AND ums.expirationDate >= :currentDate order by ums.expirationDate")
    List<UserMedicationStock> findAllByUserIdAndMedicationIdAndNotExpiredOrderedByExpirationDate(@Param("userId") UUID userId, @Param("medicationId") UUID medicationId, @Param("currentDate") OffsetDateTime currentDate);

    @Query("SELECT ums FROM UserMedicationStock ums " +
            "WHERE ums.userMedication.user.id = :userId " +
            "AND ums.userMedication.medication.id = :medicationId " +
            "AND ums.expirationDate >= :currentDate " +
            "AND (" +
            " (ums.quantityStocked * COALESCE(ums.userMedication.quantityMl, 0) - " +
            " COALESCE((SELECT SUM(u.quantityMl) FROM UserMedicationStockUsage u WHERE u.userMedicationStock.id = ums.id), 0) > 0) " +
            " OR " +
            " (ums.quantityStocked * COALESCE(ums.userMedication.quantityInt, 0) - " +
            " COALESCE((SELECT SUM(u.quantityInt) FROM UserMedicationStockUsage u WHERE u.userMedicationStock.id = ums.id), 0) > 0) " +
            ")")
    Page<UserMedicationStock> findAllValidStocksWithAvailableQuantity(
            @Param("userId") UUID userId,
            @Param("medicationId") UUID medicationId,
            @Param("currentDate") OffsetDateTime currentDate,
            Pageable pageable);


    @Query("SELECT ums FROM UserMedicationStock ums " +
            "JOIN ums.userMedication um " +
            "WHERE um.user.id = :userId " +
            "AND ums.stockedAt BETWEEN :start AND :end")
    List<UserMedicationStock> findByUserIdAndStockedAtBetween(@Param("userId") UUID userId, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);
}
