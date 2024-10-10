package ika.controllers;

import ika.controllers.aux_classes.CustomPageResponse;
import ika.controllers.aux_classes.active_ingredient.ActiveIngredientResponse;
import ika.entities.ActiveIngredient;
import ika.services.ActiveIngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/active-ingredients")
public class ActiveIngredientController {

    @Autowired
    private ActiveIngredientService activeIngredientService;

    // Endpoint para buscar todos os ingredientes ativos com filtros e paginação
    @GetMapping()
    public ResponseEntity<CustomPageResponse<ActiveIngredientResponse>> getAllActiveIngredients(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "200") int size,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(defaultValue = "description") String sortBy, // Campo de ordenação
            @RequestParam(defaultValue = "asc") String sortDirection  // Direção de ordenação
    ) {
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);
        Page<ActiveIngredientResponse> ingredientPage = activeIngredientService.getAllActiveIngredients(description, pageable);

        CustomPageResponse<ActiveIngredientResponse> customPageResponse = new CustomPageResponse<>(
                ingredientPage.getContent(),
                ingredientPage.getNumber(),
                ingredientPage.getSize(),
                ingredientPage.getSort(),
                ingredientPage.getPageable().getOffset(),
                ingredientPage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }

    // Endpoint para buscar um ingrediente ativo específico por id
    @GetMapping("/{id}")
    public ResponseEntity<ActiveIngredientResponse> getActiveIngredientById(@PathVariable UUID id) {
        ActiveIngredient activeIngredient = activeIngredientService.findById(id);
        ActiveIngredientResponse activeIngredientResponse = new ActiveIngredientResponse(activeIngredient);
        return ResponseEntity.ok(activeIngredientResponse);
    }
}
