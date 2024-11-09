package ika.services;

import ika.entities.aux_classes.user_medication_stock.AvailableStockResponse;
import ika.entities.aux_classes.user_medication_stock.UserMedicationStockResponse;
import ika.entities.UserMedication;
import ika.entities.UserMedicationStock;
import ika.entities.aux_classes.user_medication_stock.UserMedicationStockWithQuantityResponse;
import ika.repositories.UserMedicationRepository;
import ika.repositories.UserMedicationStockRepository;
import ika.repositories.UserMedicationStockUsageRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserMedicationStockService {

    @Autowired
    private UserMedicationStockRepository stockRepository;

    @Autowired
    private UserMedicationRepository userMedicationRepository;

    @Autowired
    private UserMedicationStockUsageRepository usageRepository;

    @Autowired
    private UserMedicationStockUsageService userMedicationStockUsageService;

    public UserMedicationStock addStock(UUID userMedicationId, int quantityStocked, OffsetDateTime expirationDate) {
        UserMedication userMedication = userMedicationRepository.findById(userMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("User medication not found"));

        UserMedicationStock stock = new UserMedicationStock();
        stock.setUserMedication(userMedication);
        stock.setQuantityStocked(quantityStocked);
        stock.setCreatedAt(OffsetDateTime.now());
        stock.setStockedAt(OffsetDateTime.now());
        stock.setExpirationDate(expirationDate);

        return stockRepository.save(stock);
    }

    public UserMedicationStock updateStock(UUID stockId, OffsetDateTime expirationDate) {
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

    public Page<UserMedicationStockWithQuantityResponse> getValidStocksWithAvailableQuantity(UUID userId, UUID medicationId, Pageable pageable) {
        OffsetDateTime currentDate = OffsetDateTime.now();
        Page<UserMedicationStock> validStocks = stockRepository.findAllValidStocksWithAvailableQuantity(userId, medicationId, currentDate, pageable);

        return validStocks.map(stock -> {
            Number availableQuantity = calculateAvailableQuantity(stock);
            return new UserMedicationStockWithQuantityResponse(
                    stock.getId(),
                    availableQuantity,
                    stock.getQuantityStocked(),
                    stock.getStockedAt(),
                    stock.getExpirationDate()
            );
        });
    }

    private Number calculateAvailableQuantity(UserMedicationStock stock) {
        if (stock.getUserMedication().getQuantityMl() != null && stock.getUserMedication().getQuantityMl() > 0) {
            float stockTotalMl = stock.getQuantityStocked() * stock.getUserMedication().getQuantityMl();
            float usedMl = usageRepository.sumQuantityMlByMedicationStockId(stock.getId()).orElse(0f);
            return stockTotalMl - usedMl;
        } else if (stock.getUserMedication().getQuantityInt() != null && stock.getUserMedication().getQuantityInt() > 0) {
            int stockTotalInt = stock.getQuantityStocked() * stock.getUserMedication().getQuantityInt();
            int usedInt = usageRepository.sumQuantityIntByMedicationStockId(stock.getId()).orElse(0);
            return stockTotalInt - usedInt;
        }
        return 0;
    }


    public AvailableStockResponse getAvailableStock(UUID userId, UUID medicationId) {
        // Obter todos os estoques não vencidos para a medicação e usuário especificados
        List<UserMedicationStock> validStocks = stockRepository.findAllByUserIdAndMedicationIdAndNotExpired(userId, medicationId, OffsetDateTime.now());

        if (validStocks.isEmpty()) {
            return new AvailableStockResponse("unknown", 0);
        }

        // Determinar o tipo de medicação (sólido ou líquido)
        UserMedication userMedication = validStocks.get(0).getUserMedication();

        String medicationType;
        Number totalAvailableQuantity;

        if (userMedication.getQuantityMl() != null && userMedication.getQuantityMl() > 0) {
            medicationType = "liquid";
            totalAvailableQuantity = calculateAvailableQuantityMl(validStocks);
        } else if (userMedication.getQuantityInt() != null && userMedication.getQuantityInt() > 0) {
            medicationType = "solid";
            totalAvailableQuantity = calculateAvailableQuantityInt(validStocks);
        } else {
            medicationType = "unknown";
            totalAvailableQuantity = 0;
        }

        return new AvailableStockResponse(medicationType, totalAvailableQuantity);
    }

    private Float calculateAvailableQuantityMl(List<UserMedicationStock> validStocks) {
        float totalAvailableMl = 0f;

        for (UserMedicationStock stock : validStocks) {
            int quantityStocked = stock.getQuantityStocked();
            Float quantityPerUnit = stock.getUserMedication().getQuantityMl(); // Quantidade em ml por unidade de estoque

            if (quantityPerUnit == null || quantityPerUnit <= 0) {
                continue;
            }

            // Quantidade total deste estoque
            float stockTotalMl = quantityStocked * quantityPerUnit;

            // Obter a quantidade já utilizada deste estoque usando o método existente
            Float usedMl = usageRepository.sumQuantityMlByMedicationStockId(stock.getId()).orElse(0f);

            // Quantidade disponível neste estoque
            float stockAvailableMl = stockTotalMl - usedMl;

            totalAvailableMl += stockAvailableMl;
        }

        return totalAvailableMl;
    }

    private Integer calculateAvailableQuantityInt(List<UserMedicationStock> validStocks) {
        int totalAvailableInt = 0;

        for (UserMedicationStock stock : validStocks) {
            int quantityStocked = stock.getQuantityStocked();
            Integer quantityPerUnit = stock.getUserMedication().getQuantityInt(); // Quantidade em unidades por unidade de estoque

            if (quantityPerUnit == null || quantityPerUnit <= 0) {
                continue;
            }

            // Quantidade total deste estoque
            int stockTotalInt = quantityStocked * quantityPerUnit;

            // Obter a quantidade já utilizada deste estoque usando o método existente
            Integer usedInt = usageRepository.sumQuantityIntByMedicationStockId(stock.getId()).orElse(0);
            System.out.println("Achei usage! " + usedInt);
            // Quantidade disponível neste estoque
            int stockAvailableInt = stockTotalInt - usedInt;
            System.out.println(totalAvailableInt + " + " + stockAvailableInt);

            totalAvailableInt += stockAvailableInt;
        }

        return totalAvailableInt;
    }

    public OffsetDateTime getStockForUserMedicationByUserIdAndMedicationId(UUID userId, UUID medicationId) {
        List<UserMedicationStock> validStocks = stockRepository.findAllByUserIdAndMedicationIdAndNotExpiredOrderedByExpirationDate(userId, medicationId, OffsetDateTime.now());

        if (validStocks.isEmpty()) {
            return null;
        }
        UserMedication userMedication = validStocks.get(0).getUserMedication();
        OffsetDateTime nextExpirationDate = null;
        if (userMedication.getQuantityMl() != null && userMedication.getQuantityMl() > 0) {
            nextExpirationDate = findNextExpirationDateQuantityMl(validStocks);
        } else if (userMedication.getQuantityInt() != null && userMedication.getQuantityInt() > 0) {
            nextExpirationDate = findNextExpirationDateQuantityInt(validStocks);
        }

        return nextExpirationDate;

    }

    private OffsetDateTime findNextExpirationDateQuantityMl(List<UserMedicationStock> validStocks) {
        for (UserMedicationStock stock : validStocks) {
            int quantityStocked = stock.getQuantityStocked();
            Float quantityPerUnit = stock.getUserMedication().getQuantityMl();

            if (quantityPerUnit == null || quantityPerUnit <= 0) {
                continue;
            }

            float stockTotalMl = quantityStocked * quantityPerUnit;
            Float usedMl = usageRepository.sumQuantityMlByMedicationStockId(stock.getId()).orElse(0f);
            float stockAvailableMl = stockTotalMl - usedMl;

            if (stockAvailableMl > 0) {
                return stock.getExpirationDate();
            }
        }

        return null;
    }

    private OffsetDateTime findNextExpirationDateQuantityInt(List<UserMedicationStock> validStocks) {
        for (UserMedicationStock stock : validStocks) {
            int quantityStocked = stock.getQuantityStocked();
            Integer quantityPerUnit = stock.getUserMedication().getQuantityInt();

            if (quantityPerUnit == null || quantityPerUnit <= 0) {
                continue;
            }

            int stockTotalInt = quantityStocked * quantityPerUnit;
            Integer usedInt = usageRepository.sumQuantityIntByMedicationStockId(stock.getId()).orElse(0);
            int stockAvailableInt = stockTotalInt - usedInt;

            if (stockAvailableInt > 0) {
                return stock.getExpirationDate();
            }
        }

        return null;
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
