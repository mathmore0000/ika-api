package ika.services;

import ika.controllers.aux_classes.active_ingredient.ActiveIngredientResponse;
import ika.entities.ActiveIngredient;
import ika.repositories.ActiveIngredientRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                .orElseThrow(() -> new ResourceNotFoundException("Active Ingredient not found"));
    }

    // Método para criar um novo ingrediente ativo
    public ActiveIngredient createActiveIngredient(ActiveIngredient activeIngredient) {
        return activeIngredientRepository.save(activeIngredient);
    }

    // Método para listar todos os ingredientes ativos
    public List<ActiveIngredient> getAllActiveIngredients() {
        return activeIngredientRepository.findAll();
    }

    @Autowired
    public ActiveIngredientService(ActiveIngredientRepository activeIngredientRepository) {
        this.activeIngredientRepository = activeIngredientRepository;
    }

    // Método para buscar todos os ingredientes ativos com filtros e paginação
    public Page<ActiveIngredientResponse> getAllActiveIngredients(String description, Pageable pageable) {
        return activeIngredientRepository.findAllWithFilters(description, pageable)
                .map(this::convertToActiveIngredientResponse);
    }

    // Método para buscar um ingrediente ativo específico por id
    public ActiveIngredientResponse getActiveIngredientById(UUID id) {
        ActiveIngredient ingredient = activeIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Active Ingredient not found"));
        return convertToActiveIngredientResponse(ingredient);
    }

    private ActiveIngredientResponse convertToActiveIngredientResponse(ActiveIngredient ingredient) {
        return new ActiveIngredientResponse(ingredient);
    }
}
