package ika.controllers;

import ika.controllers.aux_classes.CustomPageResponse;
import ika.controllers.aux_classes.medication.MedicationRequest;
import ika.controllers.aux_classes.medication.MedicationResponse;
import ika.services.MedicationService;
import ika.utils.GlobalValues;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@RestController
@RequestMapping("/v1/medications")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @PostMapping()
    public ResponseEntity<String> createMedication(@Valid @RequestBody MedicationRequest medicationRequest) {
        // Aqui você chama o serviço que vai salvar a entidade no banco
        medicationService.createMedication(medicationRequest);
        return ResponseEntity.ok("Medication created successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationResponse> getMedicationById(@PathVariable UUID id) {
        MedicationResponse medicationResponse = medicationService.getMedicationById(id);
        System.out.println("medicationResponse -> " + medicationResponse);
        return ResponseEntity.ok(medicationResponse);
    }
    @GetMapping()
    public ResponseEntity<CustomPageResponse<MedicationResponse>> getAllMedications(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "200") int size,
            @RequestParam(value = "name", defaultValue =  "", required = false) String name,
            @RequestParam(value = "category", defaultValue =  "", required = false) UUID categoryId,
            @RequestParam(value = "activeIngredient", defaultValue =  "", required = false) UUID activeIngredientId,
            @RequestParam(defaultValue = "name") String sortBy,  // Campo de ordenação
            @RequestParam(defaultValue = "asc") String sortDirection // Direção de ordenação
    ) {
        page = CustomPageResponse.getValidPage(page);
        size = CustomPageResponse.getValidSize(size);
        System.out.println(name + categoryId + activeIngredientId + page + size);

        // Cria um objeto Pageable com base nos parâmetros page e size
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);

        // Chama o serviço passando os parâmetros opcionais e a paginação
        Page<MedicationResponse> medicationPage = medicationService.getAllMedications(name, categoryId, activeIngredientId, pageable);

        CustomPageResponse<MedicationResponse> customPageResponse = new CustomPageResponse<>(
                medicationPage.getContent(),
                medicationPage.getNumber(),
                medicationPage.getSize(),
                medicationPage.getSort(),
                medicationPage.getPageable().getOffset(),
                medicationPage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }
}

