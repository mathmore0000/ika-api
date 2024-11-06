package ika.entities.aux_classes.usage;

import ika.entities.*;
import ika.entities.aux_classes.medication.MedicationResponse;
import lombok.Data;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class UsageWithUserResponse {
    private UUID id;
    private List<UserMedicationStockWithUsageResponse> userMedicationStockResponses;
    private Boolean isApproved;
    private User user;
    private URL url;
    private FileEntity video;
    private OffsetDateTime actionTmstamp;

    public UsageWithUserResponse(Usage usage, User user, URL url) {
        this.id = usage.getId();
        this.isApproved = usage.getIsApproved();
        this.url = url;
        this.actionTmstamp = usage.getActionTmstamp();
        this.user = user;
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
        private OffsetDateTime stockedAt; // Data de estocagem
        private OffsetDateTime expirationDate; // Data de validade

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
        private OffsetDateTime usageTimestamp; // Data do uso

        public StockUsageResponse(UserMedicationStockUsage stockUsage) {
            this.quantityUsed = stockUsage.getQuantityInt(); // ou quantityMl se aplicável
            this.usageTimestamp = stockUsage.getUsage().getActionTmstamp();
        }
    }
}
