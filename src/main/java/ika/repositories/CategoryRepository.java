package ika.repositories;

import ika.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c " +
            "WHERE COALESCE(:description, '') = '' OR LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    Page<Category> findAllWithFilters(String description, Pageable pageable);

    Optional<Category> findByDescription(String description);
}
