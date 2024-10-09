package ika.repositories;

import ika.entities.ActiveIngredient;
import ika.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActiveIngredientRepository extends JpaRepository<ActiveIngredient, UUID> {

    Optional<ActiveIngredient> findById(UUID id);

    @Query("SELECT ai FROM ActiveIngredient ai " +
            "WHERE COALESCE(:description, '') = '' OR LOWER(ai.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    Page<ActiveIngredient> findAllWithFilters(String description, Pageable pageable);
    Optional<ActiveIngredient> findByDescription(String description);
}
