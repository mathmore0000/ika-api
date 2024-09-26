package ika.auth.controllers;

import ika.auth.controllers.medication.MedicationRequest;
import ika.auth.controllers.medication.MedicationResponse;
import ika.auth.services.MedicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/medications")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @PostMapping("/create")
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
}

