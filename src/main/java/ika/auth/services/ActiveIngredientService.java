package ika.auth.services;

import ika.auth.entities.ActiveIngredient;
import ika.auth.repositories.ActiveIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActiveIngredientService {

    @Autowired
    private ActiveIngredientRepository activeIngredientRepository;

    // Método para buscar um ingrediente ativo por ID
    public ActiveIngredient findById(UUID id) {
        return activeIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Active Ingredient not found"));
    }

    // Método para criar um novo ingrediente ativo
    public ActiveIngredient createActiveIngredient(ActiveIngredient activeIngredient) {
        return activeIngredientRepository.save(activeIngredient);
    }

    // Método para listar todos os ingredientes ativos
    public List<ActiveIngredient> getAllActiveIngredients() {
        return activeIngredientRepository.findAll();
    }
}
