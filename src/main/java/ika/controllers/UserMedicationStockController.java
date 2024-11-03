package ika.controllers;

import ika.entities.aux_classes.CustomPageResponse;
import ika.entities.aux_classes.user_medication_stock.AvailableStockResponse;
import ika.entities.aux_classes.user_medication_stock.UserMedicationStockRequest;
import ika.entities.aux_classes.user_medication_stock.UserMedicationStockResponse;
import ika.entities.UserMedicationStock;
import ika.services.UserMedicationStockService;
import ika.utils.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            @Valid @RequestBody UserMedicationStockRequest request) {

        UserMedicationStock stock = stockService.addStock(request.getUserMedicationId(), request.getQuantityStocked(), request.getExpirationDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(stock);
    }

    @PatchMapping("/{stockId}")
    public ResponseEntity<UserMedicationStock> updateStock(
            @PathVariable UUID stockId,
            @RequestParam(required = false) LocalDateTime expirationDate) {

        UserMedicationStock updatedStock = stockService.updateStock(stockId, expirationDate);
        return ResponseEntity.ok(updatedStock);
    }

    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> deleteStock(@PathVariable UUID stockId) {
        stockService.deleteStock(stockId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/allStock/{medicationId}")
    public ResponseEntity<AvailableStockResponse> getAllStockForUserMedication(
            @PathVariable UUID medicationId) {

        // Obtém o ID do usuário autenticado
        UUID userId = currentUserProvider.getCurrentUserId();

        // Chama o serviço para calcular o estoque disponível
        AvailableStockResponse stock = stockService.getAvailableStock(userId, medicationId);

        return ResponseEntity.ok(stock);
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

    @GetMapping("/next-expiration/{medicationId}")
    public ResponseEntity<LocalDateTime> getNextExpirationDateForUserMedication(
            @PathVariable UUID medicationId) {

        // Obtém o ID do usuário autenticado
        UUID userId = currentUserProvider.getCurrentUserId();

        // Cria um objeto Pageable com base nos parâmetros de paginação e ordenação
        LocalDateTime nextExpirationDate = stockService.getStockForUserMedicationByUserIdAndMedicationId(userId, medicationId);

        return ResponseEntity.ok(nextExpirationDate);
    }
}
