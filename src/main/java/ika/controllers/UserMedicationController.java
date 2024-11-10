package ika.controllers;

import ika.entities.aux_classes.CustomPageResponse;
import ika.entities.aux_classes.user_medication.UserMedicationRequest;
import ika.entities.UserMedication;
import ika.services.UserMedicationService;
import ika.utils.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/user-medications")
public class UserMedicationController {

    @Autowired
    private UserMedicationService userMedicationService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @PostMapping()
    public ResponseEntity<Optional<UserMedication>> createUserMedication(@Valid @RequestBody UserMedicationRequest userMedicationDetails) {
        System.out.println(userMedicationDetails);
        UUID userId = currentUserProvider.getCurrentUserId();
        Optional<UserMedication> userMedication = userMedicationService.addUserMedication(userId, userMedicationDetails.getIdMedication(), userMedicationDetails);

        return userMedication
                .map(request -> ResponseEntity.status(HttpStatus.CREATED).body(userMedication))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());  // Conflict if duplicate
    }

    @GetMapping()
    public ResponseEntity<CustomPageResponse<UserMedication>> getAllUserMedications(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "200") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,  // Campo de ordenação
            @RequestParam(defaultValue = "asc") String sortDirection // Direção de ordenação
             ) {
        UUID userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);
        Page<UserMedication> userMedicationsPage = userMedicationService.getAllUserMedications(userId, pageable);

        CustomPageResponse<UserMedication> customPageResponse = new CustomPageResponse<>(
                userMedicationsPage.getContent(),
                userMedicationsPage.getNumber(),
                userMedicationsPage.getSize(),
                userMedicationsPage.getSort(),
                userMedicationsPage.getPageable().getOffset(),
                userMedicationsPage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }

    // New endpoint to enable or disable a user medication
    @PatchMapping("/{medicationId}/status")
    public ResponseEntity<String> updateUserMedicationStatus(
            @Valid @NotNull(message="medicationId is required") @PathVariable UUID medicationId,
            @Valid @NotNull(message="disabled is required") @RequestParam boolean disabled) {
        UUID userId = currentUserProvider.getCurrentUserId();
        UserMedication updatedMedication  = userMedicationService.updateUserMedicationStatus(userId, medicationId, disabled);
        return ResponseEntity.ok("User medication status updated successfully");
    }

    // New endpoint to update a user medication
    @PutMapping("/{medicationId}")
    public ResponseEntity<UserMedication> updateUserMedication(
            @Valid @NotNull(message="medicationId is required") @PathVariable UUID medicationId,
            @Valid @RequestBody UserMedicationRequest updatedRequest) {
        UUID userId = currentUserProvider.getCurrentUserId();
        UserMedication updatedMedication = userMedicationService.updateUserMedication(userId, medicationId, updatedRequest);
        return ResponseEntity.ok(updatedMedication);
    }
}
