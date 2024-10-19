package ika.controllers;

import ika.entities.Usage;
import ika.entities.aux_classes.CustomPageResponse;
import ika.entities.aux_classes.usage.UsageRequest;
import ika.services.UsageService;
import ika.utils.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/usages")
public class UsageController {

    @Autowired
    private UsageService usageService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    // POST /usages: Create new medication usage with video
    @PostMapping()
    public ResponseEntity<Map<String, String>> createUsage(
            @Valid @RequestParam("file") @NotNull(message = "File is required") MultipartFile file,
            @Valid @RequestPart UsageRequest usageRequest) throws Exception {

        UUID userId = currentUserProvider.getCurrentUserId();
        Map<String, String> response = usageService.createUsage(userId, file, usageRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<CustomPageResponse<Usage>> getFilteredUsages(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "isApproved", required = false) Boolean isApproved,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "actionTmstamp") String sortBy,  // Campo de ordenação, por padrão "actionTmstamp"
            @RequestParam(defaultValue = "asc") String sortDirection // Direção de ordenação, por padrão "asc"
    ) {
        // Valida e ajusta os parâmetros de paginação, se necessário
        page = CustomPageResponse.getValidPage(page);
        size = CustomPageResponse.getValidSize(size);

        // Cria o Pageable com base nos parâmetros de paginação e ordenação
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);

        // Chama o serviço passando os filtros e a paginação
        Page<Usage> usagePage = usageService.getFilteredUsages(isApproved, fromDate, toDate, pageable);

        // Cria a resposta customizada para retornar
        CustomPageResponse<Usage> customPageResponse = new CustomPageResponse<>(
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
//    @DeleteMapping("/{usageId}")
//    public ResponseEntity<String> deleteUsage(@PathVariable UUID usageId) {
//        UUID userId = currentUserProvider.getCurrentUserId();
//        usageService.deleteUsage(userId, usageId);
//        return ResponseEntity.ok("Usage deleted successfully");
//    }
}