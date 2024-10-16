package ika.controllers;

import ika.controllers.aux_classes.CustomPageResponse;
import ika.controllers.aux_classes.user_medication_stock.UserMedicationStockResponse;
import ika.entities.UserMedicationStock;
import ika.services.UserMedicationStockService;
import ika.utils.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/user-medication-stocks")
public class UserMedicationStockController {

    @Autowired
    private UserMedicationStockService stockService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<UserMedicationStock> createStock(
            @RequestParam UUID userMedicationId,
            @RequestParam int quantityStocked,
            @RequestParam LocalDateTime expirationDate,
            @RequestParam(required = false) Integer quantityCard) {

        UserMedicationStock stock = stockService.addStock(userMedicationId, quantityStocked, expirationDate, quantityCard);
        return ResponseEntity.status(HttpStatus.CREATED).body(stock);
    }

    @PatchMapping("/{stockId}")
    public ResponseEntity<UserMedicationStock> updateStock(
            @PathVariable UUID stockId,
            @RequestParam(required = false) Integer quantityCard,
            @RequestParam(required = false) LocalDateTime expirationDate) {

        UserMedicationStock updatedStock = stockService.updateStock(stockId, quantityCard, expirationDate);
        return ResponseEntity.ok(updatedStock);
    }

    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> deleteStock(@PathVariable UUID stockId) {
        stockService.deleteStock(stockId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{medicationId}")
    public ResponseEntity<CustomPageResponse<UserMedicationStockResponse>> getStockForUserMedication(
            @PathVariable UUID medicationId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection) {

        // Obtém o ID do usuário autenticado
        UUID userId = currentUserProvider.getCurrentUserId();

        // Cria um objeto Pageable com base nos parâmetros de paginação e ordenação
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);

        // Chama o serviço para buscar o estoque com paginação e ordenação
        Page<UserMedicationStockResponse> stockPage = stockService.getStockForUserMedicationByIdUserIdAndMedication(userId, medicationId, pageable);

        // Prepara a resposta personalizada de página
        CustomPageResponse<UserMedicationStockResponse> customPageResponse = new CustomPageResponse<>(
                stockPage.getContent(),
                stockPage.getNumber(),
                stockPage.getSize(),
                stockPage.getSort(),
                stockPage.getPageable().getOffset(),
                stockPage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }
}
