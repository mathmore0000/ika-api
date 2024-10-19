package ika.repositories;

import ika.entities.UserMedicationStockUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserMedicationStockUsageRepository extends JpaRepository<UserMedicationStockUsage, UUID> {
}
