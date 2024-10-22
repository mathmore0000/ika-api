package ika.entities.aux_classes.usage;

import ika.entities.FileEntity;
import ika.entities.Usage;
import ika.entities.UserMedicationStock;
import ika.entities.UserMedicationStockUsage;
import ika.entities.aux_classes.medication.MedicationResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class UsageResponse {
    private UUID id;
    private List<UserMedicationStockWithUsageResponse> userMedicationStockResponses;
    private Boolean isApproved;
    private FileEntity video;
    private LocalDateTime actionTmstamp;

    public UsageResponse(Usage usage) {
        this.id = usage.getId();
        this.isApproved = usage.getIsApproved();
        this.actionTmstamp = usage.getActionTmstamp();
        this.video = usage.getVideo();

        // Aqui, mapeamos os estoques de medicamentos independentemente de haverem ou não usos
        this.userMedicationStockResponses = usage.getUserMedicationStockUsages().stream()
                .map(UserMedicationStockUsage::getUserMedicationStock)
                .distinct() // Evita duplicados caso existam múltiplos usos para o mesmo estoque
                .map(UserMedicationStockWithUsageResponse::new)
                .collect(Collectors.toList());
    }

    @Data
    public static class UserMedicationStockWithUsageResponse {
        private UUID id; // ID do estoque de medicação
        private MedicationResponse medicationResponse; // Medicamento associado
        private Integer quantityStocked; // Quantidade em estoque
        private LocalDateTime stockedAt; // Data de estocagem
        private LocalDateTime expirationDate; // Data de validade

        private List<StockUsageResponse> usages; // Lista de usos para este estoque

        public UserMedicationStockWithUsageResponse(UserMedicationStock userMedicationStock) {
            this.id = userMedicationStock.getId();
            this.quantityStocked = userMedicationStock.getQuantityStocked();
            this.stockedAt = userMedicationStock.getStockedAt();
            this.expirationDate = userMedicationStock.getExpirationDate();

            // Adicionando a resposta do medicamento relacionada ao UserMedicationStock
            this.medicationResponse = new MedicationResponse(userMedicationStock.getUserMedication().getMedication());

            // Garantimos que, mesmo sem usos, retornamos uma lista vazia
            this.usages = userMedicationStock.getStockUsages() != null ?
                    userMedicationStock.getStockUsages().stream()
                            .map(StockUsageResponse::new)
                            .collect(Collectors.toList())
                    : List.of(); // Retorna lista vazia se não houver usos
        }
    }

    @Data
    public static class StockUsageResponse {
        private Integer quantityUsed; // Quantidade usada
        private LocalDateTime usageTimestamp; // Data do uso

        public StockUsageResponse(UserMedicationStockUsage stockUsage) {
            this.quantityUsed = stockUsage.getQuantityInt(); // ou quantityMl se aplicável
            this.usageTimestamp = stockUsage.getUsage().getActionTmstamp();
        }
    }
}