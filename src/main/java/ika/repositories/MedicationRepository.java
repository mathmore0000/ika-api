package ika.repositories;

import ika.entities.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    Optional<Medication> findById(UUID id);

    @Query("SELECT m FROM Medication m " +
            "WHERE (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:categoryId IS NULL OR m.category.id = :categoryId) " +
            "AND (:activeIngredientId IS NULL OR m.activeIngredient.id = :activeIngredientId)" +
            "AND (m.isValid = TRUE)" +
            "AND (m.disabled = FALSE)" +
            "OR (m.user.id = :userId)")
    Page<Medication> findAllWithFilters(String name, UUID categoryId, UUID activeIngredientId, UUID userId, Pageable pageable);
}
