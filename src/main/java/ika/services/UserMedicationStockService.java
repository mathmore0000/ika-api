package ika.services;

import ika.entities.aux_classes.user_medication_stock.UserMedicationStockResponse;
import ika.entities.UserMedication;
import ika.entities.UserMedicationStock;
import ika.repositories.UserMedicationRepository;
import ika.repositories.UserMedicationStockRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserMedicationStockService {

    @Autowired
    private UserMedicationStockRepository stockRepository;

    @Autowired
    private UserMedicationRepository userMedicationRepository;

    public UserMedicationStock addStock(UUID userMedicationId, int quantityStocked, LocalDateTime expirationDate) {
        UserMedication userMedication = userMedicationRepository.findById(userMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("User medication not found"));

        UserMedicationStock stock = new UserMedicationStock();
        stock.setUserMedication(userMedication);
        stock.setQuantityStocked(quantityStocked);
        stock.setCreatedAt(LocalDateTime.now());
        stock.setStockedAt(LocalDateTime.now());
        stock.setExpirationDate(expirationDate);

        return stockRepository.save(stock);
    }

    public UserMedicationStock updateStock(UUID stockId, LocalDateTime expirationDate) {
        UserMedicationStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        stock.setExpirationDate(expirationDate);

        return stockRepository.save(stock);
    }

    public void deleteStock(UUID stockId) {
        UserMedicationStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        stockRepository.delete(stock);
    }

    public Page<UserMedicationStockResponse> getStockForUserMedicationByIdUserIdAndMedication(UUID userId, UUID medicationId, Pageable pageable) {
        Page<UserMedicationStock> stocks = stockRepository.findAllByUserIdAndMedicationId(userId, medicationId, pageable);
        return stocks.map(stock -> new UserMedicationStockResponse(
                stock.getId(),
                stock.getQuantityStocked(),
                stock.getStockedAt(),
                stock.getExpirationDate()
        ));
    }

    public List<UserMedicationStock> getUserMedicationStocksByIdUserIdAndMedications(UUID userId, List<UUID> medicationIds) {
        List<UserMedicationStock> stocks = stockRepository.findAllByUserIdAndMedicationIds(userId, medicationIds);

        return stocks;
    }
}
