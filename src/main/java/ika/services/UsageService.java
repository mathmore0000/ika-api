package ika.services;

import ika.entities.FileEntity;
import ika.entities.Usage;
import ika.entities.UserMedicationStock;
import ika.entities.aux_classes.usage.UsageRequest;
import ika.repositories.UsageRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UsageService {

    @Autowired
    private UsageRepository usageRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserMedicationStockUsageService userMedicationStockLogUsageService;

    @Autowired
    private UserMedicationStockService userMedicationStockService;

    public Map<String, String> createUsage(UUID userId, MultipartFile file, UsageRequest usageRequest) throws Exception {
        // Obter os IDs dos estoques de medicação do request
        List<UUID> medicationStockIds = usageRequest.getMedications().stream()
                .map(UsageRequest.MedicationStockRequest::getMedicationStockId)
                .toList();

        // Validar e obter os estoques de medicação do usuário
        List<UserMedicationStock> userMedicationStocks = userMedicationStockService.getUserMedicationStocksByIdUserIdAndMedications(userId, medicationStockIds);
        if (userMedicationStocks.size() != medicationStockIds.size()) {
            throw new ResourceNotFoundException("Some medications could not be found for the user");
        }
        System.out.println("Mecicamentos validados");

        // Upload do vídeo
        FileEntity video = fileService.uploadFile("videos", file);
        System.out.println("Vídeo upado no S3 e salvo na base");
        System.out.println(video);

        // Criar e salvar o uso
        Usage usageToInsert = new Usage();
        usageToInsert.setId(UUID.randomUUID());
        usageToInsert.setUserId(userId);
        usageToInsert.setVideo(video);
        usageToInsert.setIsApproved(null);  // Inicialmente não aprovado
        usageToInsert.setActionTmstamp(usageRequest.getActionTmstamp());
        Usage usage = usageRepository.save(usageToInsert);
        System.out.println("Usage salva");
        System.out.println(usage);

        // Passar os estoques de medicação já validados para o serviço
        userMedicationStockLogUsageService.createMedicationLog(usage.getId(), usageRequest.getMedications(), userMedicationStocks);

        return Map.of(
                "message", "Usage created successfully",
                "usageId", usage.getId().toString(),
                "videoId", video.getId().toString()
        );
    }


    public Page<Usage> getFilteredUsages(Boolean isApproved, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        // O repositório fará a filtragem com base nos parâmetros e na paginação
        return usageRepository.findAllWithFilters(isApproved, fromDate, toDate, pageable);
    }

//    public void deleteUsage(UUID userId, UUID usageId) {
//        // Find usage by id
//        Usage usage = usageRepository.findByIdAndUserId(usageId, userId)
//                .orElseThrow(() -> new ResourceNotFoundException("Usage not found or you are not authorized to delete it"));
//
//        // Delete video
//        fileService.deleteFile());
//
//        // Delete the usage and associations
//        usageRepository.delete(usage);
//    }
}
