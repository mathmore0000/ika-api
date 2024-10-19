package ika.repositories;

import ika.entities.UserMedicationStockUsage;
import ika.services.UserMedicationStockUsageService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMedicationStockUsageRepository extends JpaRepository<UserMedicationStockUsage, UUID> {
    @Query("SELECT SUM(u.quantityMl) FROM UserMedicationStockUsage u WHERE u.userMedicationStock.id = :medicationStockId")
    Optional<Float> sumQuantityMlByMedicationStockId(@Param("medicationStockId") UUID medicationStockId);

    @Query("SELECT SUM(u.quantityInt) FROM UserMedicationStockUsage u WHERE u.userMedicationStock.id = :medicationStockId")
    Optional<Integer> sumQuantityIntByMedicationStockId(@Param("medicationStockId") UUID medicationStockId);

    List<UserMedicationStockUsage> findByUsageId(UUID usageId);
}
