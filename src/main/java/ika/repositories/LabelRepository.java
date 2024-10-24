package ika.repositories;

import ika.entities.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<Label, UUID> {
    @Query("SELECT l FROM Label l " +
            "WHERE COALESCE(:description, '') = '' OR LOWER(l.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    Page<Label> findAllWithFilters(String description, Pageable pageable);

    Optional<Label> findByDescription(String description);
}
