package ika.auth.repositories;

import ika.auth.entities.ActiveIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActiveIngredientRepository extends JpaRepository<ActiveIngredient, UUID> {
}
