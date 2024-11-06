package ika.controllers;

import ika.entities.Usage;
import ika.entities.aux_classes.CustomPageResponse;
import ika.entities.aux_classes.usage.ApproveRejectUsageRequest;
import ika.entities.aux_classes.usage.UsageRequest;
import ika.entities.aux_classes.usage.UsageResponse;
import ika.entities.aux_classes.usage.UsageWithUserResponse;
import ika.services.UsageService;
import ika.utils.CurrentUserProvider;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/v1/usages")
public class UsageController {

    @Autowired
    private UsageService usageService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private Validator validator;
    // POST /usages: Create new medication usage with video
    @PostMapping()
    public ResponseEntity<Map<String, String>> createUsage(
            @Valid @RequestParam("file") @NotNull(message = "File is required") MultipartFile file,
            @RequestPart UsageRequest usageRequest) throws Exception {
        System.out.println("entrei");
        Set<ConstraintViolation<UsageRequest>> violations = validator.validate(usageRequest);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            for (ConstraintViolation<UsageRequest> violation : violations) {
                String fieldName = violation.getPropertyPath().toString();
                String errorMessage = violation.getMessage();
                errors.put(fieldName, errorMessage);
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        UUID userId = currentUserProvider.getCurrentUserId();
        Map<String, String> response = usageService.createUsage(userId, file, usageRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<CustomPageResponse<UsageResponse>> getFilteredUsagesByUser(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "isApproved", required = false) Boolean isApproved,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
            @RequestParam(defaultValue = "actionTmstamp") String sortBy,  // Campo de ordenação, por padrão "actionTmstamp"
            @RequestParam(defaultValue = "asc") String sortDirection // Direção de ordenação, por padrão "asc"
    ) {
        UUID userId = currentUserProvider.getCurrentUserId();
        // Valida e ajusta os parâmetros de paginação, se necessário
        page = CustomPageResponse.getValidPage(page);
        size = CustomPageResponse.getValidSize(size);

        // Cria o Pageable com base nos parâmetros de paginação e ordenação
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);

        // Chama o serviço passando os filtros e a paginação
        Page<UsageResponse> usagePage = usageService.getFilteredUsagesByUser(userId, isApproved, fromDate, toDate, pageable);

        // Cria a resposta customizada para retornar
        CustomPageResponse<UsageResponse> customPageResponse = new CustomPageResponse<>(
                usagePage.getContent(),
                usagePage.getNumber(),
                usagePage.getSize(),
                usagePage.getSort(),
                usagePage.getPageable().getOffset(),
                usagePage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }

    @GetMapping("/responsible")
    public ResponseEntity<CustomPageResponse<UsageWithUserResponse>> getFilteredUsagesByResponsible(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "isApproved", required = false) Boolean isApproved,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
            @RequestParam(defaultValue = "actionTmstamp") String sortBy,  // Campo de ordenação, por padrão "actionTmstamp"
            @RequestParam(defaultValue = "asc") String sortDirection // Direção de ordenação, por padrão "asc"
    ) {
        UUID responsibleId = currentUserProvider.getCurrentUserId();
        // Valida e ajusta os parâmetros de paginação, se necessário
        page = CustomPageResponse.getValidPage(page);
        size = CustomPageResponse.getValidSize(size);

        // Cria o Pageable com base nos parâmetros de paginação e ordenação
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);

        // Chama o serviço passando os filtros e a paginação
        Page<UsageWithUserResponse> usagePage = usageService.getFilteredUsagesByResponsible(responsibleId, isApproved, fromDate, toDate, pageable);

        // Cria a resposta customizada para retornar
        CustomPageResponse<UsageWithUserResponse> customPageResponse = new CustomPageResponse<>(
                usagePage.getContent(),
                usagePage.getNumber(),
                usagePage.getSize(),
                usagePage.getSort(),
                usagePage.getPageable().getOffset(),
                usagePage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }

    // DELETE /usage/{usageId}: Delete a usage with verification of user_id
    @DeleteMapping("/{usageId}")
    public ResponseEntity<String> deleteUsage(@PathVariable UUID usageId) {
        UUID userId = currentUserProvider.getCurrentUserId();
        usageService.deleteUsage(userId, usageId);
        return ResponseEntity.ok("Usage deleted successfully");
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveUsage(@PathVariable UUID id, @Valid @RequestBody ApproveRejectUsageRequest request) {
        try {
            usageService.updateUsage(id, request, true);
            return ResponseEntity.ok("Usage approved successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectUsage(@PathVariable UUID id, @Valid @RequestBody ApproveRejectUsageRequest request) {
        try {
            usageService.updateUsage(id, request, false);
            return ResponseEntity.ok("Usage rejected successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
