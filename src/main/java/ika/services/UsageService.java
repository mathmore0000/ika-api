package ika.services;

import ika.entities.*;
import ika.entities.aux_classes.usage.ApproveRejectUsageRequest;
import ika.entities.aux_classes.usage.UsageRequest;
import ika.entities.aux_classes.usage.UsageResponse;
import ika.entities.aux_classes.usage.UsageWithUserResponse;
import ika.repositories.*;
import ika.utils.CurrentUserProvider;
import ika.utils.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsageService {

    @Autowired
    private UsageRepository usageRepository;

    @Autowired
    private UsageLabelsRepository usageLabelsRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private UserResponsibleRepository userResponsibleRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserMedicationStockUsageService userMedicationStockLogUsageService;

    @Autowired
    private UserMedicationStockService userMedicationStockService;

    public Map<String, String> createUsage(User user, MultipartFile file, UsageRequest usageRequest) throws Exception {
        // Obter os IDs dos estoques de medicação do request
        List<UUID> medicationStockIds = usageRequest.getMedications().stream()
                .map(UsageRequest.MedicationStockRequest::getMedicationStockId)
                .toList();

        // Validar e obter os estoques de medicação do usuário
        List<UserMedicationStock> userMedicationStocks = userMedicationStockService.getUserMedicationStocksByIdUserIdAndMedications(user.getId(), medicationStockIds);
        if (userMedicationStocks.size() != medicationStockIds.size()) {
            throw new ResourceNotFoundException("Some medications could not be found for the user");
        }
        validateUserMedicationUsages(userMedicationStocks, usageRequest);
        System.out.println("Mecicamentos validados");

        // Upload do vídeo
        FileEntity video = fileService.uploadVideo(user.getId(), "videos", file);
        System.out.println("Vídeo upado no S3 e salvo na base");
        System.out.println(video);

        // Criar e salvar o uso
        Usage usageToInsert = new Usage();
        usageToInsert.setId(UUID.randomUUID());
        usageToInsert.setUser(user);
        usageToInsert.setVideo(video);
        usageToInsert.setIsApproved(null);  // Inicialmente não aprovado
        usageToInsert.setActionTmstamp(usageRequest.getActionTmstamp());
        Usage usage = usageRepository.save(usageToInsert);
        System.out.println("Usage salva");
        System.out.println(usage);

        // Passar os estoques de medicação já validados para o serviço
        userMedicationStockLogUsageService.createMedicationLog(usage.getId(), usageRequest.getMedications(), userMedicationStocks);

        createNotificationOnNewUsage(user);
        return Map.of(
                "message", "Usage created successfully",
                "usageId", usage.getId().toString(),
                "videoId", video.getId().toString()
        );
    }

    private void createNotificationOnNewUsage(User user) {
        List<UserResponsible> userResponsibles = userResponsibleRepository.findByUserIdAndAccepted(user.getId(), true);
        if (userResponsibles.isEmpty()) {
            return;
        }
        for (UserResponsible userResponsible : userResponsibles) {
            notificationService.createNotification(userResponsible.getResponsible(), "Novo vídeo", "{\"message\": \"Novo vídeo do usuário " + user.getDisplayName() + " necessitando validação.\"}");
        }
    }

    public void updateUsage(UUID usageId, ApproveRejectUsageRequest request, boolean isApproved) throws Exception {
        Optional<Usage> usageOptional = usageRepository.findById(usageId);
        if (usageOptional.isEmpty()) {
            throw new ResourceNotFoundException("Usage not found.");
        }

        Usage usage = usageOptional.get();
        UUID responsibleId = currentUserProvider.getCurrentUserId();

        if (!userResponsibleRepository.existsByUserIdAndResponsibleIdAndAccepted(usage.getUser().getId(), responsibleId)) {
            throw new ResourceNotFoundException("You are not responsible by this usage.");
        }

        usage.setIsApproved(isApproved);
        usage.setResponsible(currentUserProvider.getCurrentUser());
        usage.setObs(request.getObs());

        // Remover as associações antigas de UsageLabels
        List<UUID> usageLabelIds = usageLabelsRepository.findByUsage_Id(usageId).stream()
                .map(UsageLabels::getId)
                .collect(Collectors.toList());

        // Criar novas associações de UsageLabels
        Set<UsageLabels> newUsageLabels = new HashSet<>();
        for (UUID labelId : request.getLabels()) {
            Label label = labelRepository.findById(labelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Some labels could not be found."));
            UsageLabels usageLabel = new UsageLabels(UUID.randomUUID(), label, usage);  // Criar novo UsageLabels
            newUsageLabels.add(usageLabel);
        }

        usageLabelsRepository.deleteAllById(usageLabelIds);
        usageLabelsRepository.saveAll(newUsageLabels);  // Salvar as novas associações
        usageRepository.save(usage);  // Salvar a entidade Usage atualizada

        notificationService.createNotification(usage.getUser(), "Vídeo " + (isApproved ? "aprovado" : "reprovado"), "{\"message\": \"O usuário " + usage.getResponsible().getDisplayName() + " " + (isApproved ? "aprovou" : "reprovou") + " seu vídeo.\"}");
    }

    private void validateUserMedicationUsages(List<UserMedicationStock> userMedicationStocks, UsageRequest usageRequest) {
        for (UsageRequest.MedicationStockRequest medicationRequest : usageRequest.getMedications()) {
            UUID medicationStockId = medicationRequest.getMedicationStockId();
            UserMedicationStock matchingStock = userMedicationStocks.stream()
                    .filter(stock -> stock.getId().equals(medicationStockId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Medication stock not found for the given medicationStockId: " + medicationStockId));

            UserMedication userMedication = matchingStock.getUserMedication();

            // Obter valores do request para validação
            Float quantityMl = medicationRequest.getQuantityMl();
            Integer quantityInt = medicationRequest.getQuantityInt();

            // Primeiro, valida se os valores inseridos estão corretos em relação ao medicamento
            validateMedicationUsage(userMedication, quantityMl, quantityInt);

            // Validar se a quantidade solicitada não ultrapassa a quantidade disponível em estoque
            validateStockAvailability(matchingStock, userMedication, quantityMl, quantityInt);

            // Exibir informações para debug
            System.out.println("Validando " + userMedication.getMedication().getName());
        }
    }

    private void validateStockAvailability(UserMedicationStock userMedicationStock, UserMedication userMedication, Float quantityMl, Integer quantityInt) {
        // Calcula a quantidade disponível de acordo com o estoque atual
        int quantityStocked = userMedicationStock.getQuantityStocked();

        // Verifica o tipo de medicamento
        if (userMedication.getMedication().getQuantityMl() != null && userMedication.getMedication().getQuantityMl() > 0) {
            // Se for medicamento líquido
            float totalAvailableMl = quantityStocked * userMedication.getQuantityMl();

            // Subtrai a quantidade já utilizada
            float usedMl = userMedicationStockLogUsageService.getTotalUsedMlForStock(userMedicationStock.getId());

            float remainingMl = totalAvailableMl - usedMl;

            // Verifica se a quantidade solicitada está disponível
            if (quantityMl != null && quantityMl > remainingMl) {
                throw new IllegalArgumentException("The requested quantityMl exceeds the available stock.");
            }
        } else if (userMedication.getMedication().getQuantityInt() != null && userMedication.getMedication().getQuantityInt() > 0) {
            // Se for medicamento sólido
            int totalAvailableInt = quantityStocked * userMedication.getQuantityInt();

            // Subtrai a quantidade já utilizada
            int usedInt = userMedicationStockLogUsageService.getTotalUsedIntForStock(userMedicationStock.getId());

            int remainingInt = totalAvailableInt - usedInt;

            // Verifica se a quantidade solicitada está disponível
            if (quantityInt != null && quantityInt > remainingInt) {
                throw new IllegalArgumentException("The requested quantityInt exceeds the available stock.");
            }
        }
    }

    private void validateMedicationUsage(UserMedication userMedication, Float quantityMl, Integer quantityInt) {
        if (userMedication.getMedication().getQuantityMl() != null && userMedication.getMedication().getQuantityMl() > 0) {
            // Validar quantidade em ml
            if (quantityMl == null || quantityMl <= 0) {
                throw new IllegalArgumentException("The quantityMl must be greater than 0 for liquid medications.");
            }
        } else if (userMedication.getMedication().getQuantityInt() != null && userMedication.getMedication().getQuantityInt() > 0) {
            // Validar quantidade em int (comprimidos)
            if (quantityInt == null || quantityInt <= 0) {
                throw new IllegalArgumentException("The quantityInt must be greater than 0 for solid medications.");
            }
        } else {
            // Caso nenhum dos valores seja aplicável
            throw new IllegalArgumentException("No valid quantity defined for this medication.");
        }
    }

    public Page<UsageResponse> getFilteredUsagesByUser(UUID userId, Boolean isApproved, OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable) {
        // O repositório fará a filtragem com base nos parâmetros e na paginação
        return usageRepository.findAllWithFiltersByUserId(userId, isApproved, fromDate, toDate, pageable).map(this::convertToUsageResponse);
    }

    public Page<UsageWithUserResponse> getFilteredUsagesByResponsible(UUID responsibled, Boolean isApproved, OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable) {
        // O repositório fará a filtragem com base nos parâmetros e na paginação
        return usageRepository.findAllWithFiltersByResponsibleId(responsibled, isApproved, fromDate, toDate, pageable).map(this::convertToUsageWithUserResponse);
    }

    @Transactional
    public void deleteUsage(UUID userId, UUID usageId) {
        // Buscar o usage pelo id e validar o userId
        Usage usage = usageRepository.findByIdAndUserId(usageId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usage not found or you are not authorized to delete it"));

        // Verificar se o usage está aprovado
        if (usage.getIsApproved() != null && usage.getIsApproved()) {
            throw new IllegalArgumentException("Usage cannot be deleted because it has already been approved");
        }

        // Deletar os logs de uso da medicação associados
        userMedicationStockLogUsageService.deleteLogsByUsageId(usageId);

        usageLabelsRepository.deleteByUsageId(usageId);

        // Deletar o usage
        usageRepository.delete(usage);

        // Deletar o vídeo associado (tanto na base quanto no S3)
        if (usage.getVideo() != null) {
            fileService.deleteFile(usage.getVideo().getId());  // Deletar o registro na base
        }
    }

    private UsageResponse convertToUsageResponse(Usage usage) {
        URL url = fileService.generatePresignedUrl(usage.getVideo().getBucket().getName(), usage.getVideo().getName());
        return new UsageResponse(usage, url);
    }

    private UsageWithUserResponse convertToUsageWithUserResponse(Usage usage) {
        Optional<User> user = userRepository.findById(usage.getUser().getId());
        URL url = fileService.generatePresignedUrl(usage.getVideo().getBucket().getName(), usage.getVideo().getName());
        return new UsageWithUserResponse(usage, user.get(), url);
    }
}
