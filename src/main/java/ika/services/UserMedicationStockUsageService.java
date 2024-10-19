package ika.services;

import ika.entities.UserMedicationStockUsage;
import ika.entities.Usage;
import ika.entities.UserMedicationStock;
import ika.entities.aux_classes.usage.UsageRequest;
import ika.repositories.UserMedicationStockUsageRepository;
import ika.repositories.UsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserMedicationStockUsageService {

    @Autowired
    private UserMedicationStockUsageRepository logUsageRepository;

    @Autowired
    private UserMedicationStockUsageRepository userMedicationStockUsageRepository;

    @Autowired
    private UsageRepository usageRepository;

    public void createMedicationLog(UUID usageId, List<UsageRequest.MedicationStockRequest> medications, List<UserMedicationStock> userMedicationStocks) {
        Usage usage = usageRepository.findById(usageId)
                .orElseThrow(() -> new RuntimeException("Usage not found"));

        for (UsageRequest.MedicationStockRequest medicationRequest : medications) {
            // Encontrar o estoque de medicação correspondente pelo ID no userMedicationStocks
            UserMedicationStock matchedStock = userMedicationStocks.stream()
                    .filter(stock -> stock.getId().equals(medicationRequest.getMedicationStockId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Medication stock not found"));

            // Criar o log de uso de medicação
            UserMedicationStockUsage log = new UserMedicationStockUsage();
            log.setId(UUID.randomUUID());
            log.setUsage(usage);  // Associar ao uso
            log.setUserMedicationStock(matchedStock);  // Associar o estoque de medicação já recuperado
            log.setQuantityInt(medicationRequest.getQuantityInt());
            log.setQuantityMl(medicationRequest.getQuantityMl());

            logUsageRepository.save(log);
        }
    }

    public void deleteLogsByUsageId(UUID usageId) {
        List<UserMedicationStockUsage> logs = logUsageRepository.findByUsageId(usageId);
        logUsageRepository.deleteAll(logs);
    }

    public float getTotalUsedMlForStock(UUID medicationStockId) {
        // Query the repository to get the total quantity of medication (in ml) used from the stock
        return userMedicationStockUsageRepository.sumQuantityMlByMedicationStockId(medicationStockId).orElse(0f);
    }

    public int getTotalUsedIntForStock(UUID medicationStockId) {
        // Query the repository to get the total number of pills used from the stock
        return userMedicationStockUsageRepository.sumQuantityIntByMedicationStockId(medicationStockId).orElse(0);
    }
}
