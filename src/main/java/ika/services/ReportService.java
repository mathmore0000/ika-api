// ReportServiceImpl.java
package ika.services;

import ika.entities.*;
import ika.repositories.*;
import ika.utils.exceptions.ResourceNotFoundException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private UserMedicationRepository userMedicationRepository;

    @Autowired
    private UsageRepository usageRepository;

    @Autowired
    private UserMedicationStockRepository userMedicationStockRepository;

    @Autowired
    private UserResponsibleRepository userResponsibleRepository;

    // Caminho para a imagem padrão
    String defaultImagePath = "/images/default-user-image.jpg";

    public ByteArrayInputStream generateUserReport(User user, int year, int month) {
        try {
            // Configurar parâmetros para filtrar por data
            OffsetDateTime startDate = OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            OffsetDateTime endDate = (startDate.getMonth() == OffsetDateTime.now(ZoneOffset.UTC).getMonth() && startDate.getYear() == OffsetDateTime.now(ZoneOffset.UTC).getYear()) ? OffsetDateTime.now(ZoneOffset.UTC) : startDate.plusMonths(1).minusSeconds(1);

            if (endDate.isAfter(OffsetDateTime.now())) {
                throw new ResourceNotFoundException("No data found on user");
            }
            if (startDate.isAfter(OffsetDateTime.now())) {
                throw new ResourceNotFoundException("No data found on user");
            }
            InputStream jrxmlInput = getClass().getResourceAsStream("/templates/reports/user/UserReportTemplate.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInput);

            InputStream userMedicationStockSubReportInput = getClass().getResourceAsStream("/templates/reports/user/UserMedicationStockSubReport.jrxml");
            JasperReport userMedicationStockSubReport = JasperCompileManager.compileReport(userMedicationStockSubReportInput);

            InputStream usageApprovalRejectSubReportInput = getClass().getResourceAsStream("/templates/reports/UsageApprovalsRejectionsSubReport.jrxml");
            JasperReport usageApprovalRejectSubReport = JasperCompileManager.compileReport(usageApprovalRejectSubReportInput);

            InputStream medicationUsageSubReportInput = getClass().getResourceAsStream("/templates/reports/user/MedicationUsageSubReport.jrxml");
            JasperReport medicationUsageSubReport = JasperCompileManager.compileReport(medicationUsageSubReportInput);

            // Formatar a data
            SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
            DateTimeFormatter formatterDatetime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String birthDate = (user.getBirthDate() != null) ? formatterDate.format(user.getBirthDate()) : "N/A";
            String todayDatetime = formatterDatetime.format(OffsetDateTime.now(ZoneOffset.UTC));

            // Parâmetros do relatório
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("userImage", getUserImage(user.getAvatarUrl()));
            parameters.put("displayName", user.getDisplayName());
            parameters.put("email", user.getEmail());
            parameters.put("birthDate", birthDate);
            parameters.put("todayDatetime", todayDatetime);

            // Configurar dados do sub-relatório
            List<UserMedicationStock> stocks = userMedicationStockRepository.findByUserIdAndStockedAtBetween(user.getId(), startDate, endDate);
            List<Map<String, Object>> stockDataList = new ArrayList<>();

            for (UserMedicationStock stock : stocks) {
                Map<String, Object> stockData = new HashMap<>();
                stockData.put("medicationName", stock.getUserMedication().getMedication().getName());
                stockData.put("quantityStocked", stock.getQuantityStocked());
                stockData.put("stockingDate", stock.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                stockData.put("expirationDate", stock.getExpirationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                stockDataList.add(stockData);
            }

            JRBeanCollectionDataSource stockDataSource = new JRBeanCollectionDataSource(stockDataList);
            parameters.put("userMedicationStockSubReport", userMedicationStockSubReport);
            parameters.put("userMedicationStockDateSource", stockDataSource);


            // Usos corretos e incorretos
            List<Usage> usages = usageRepository.findByUserIdAndActionTmstampBetween(user.getId(), startDate, endDate);
            List<Map<String, Object>> usageDataList = new ArrayList<>();

            for (Usage us : usages) {
                Map<String, Object> usageData = new HashMap<>();
                usageData.put("medicationName", us.getUserMedicationStockUsages().get(0).getUserMedicationStock().getUserMedication().getMedication().getName());
                usageData.put("ingestionTime", formatterDatetime.format(us.getActionTmstamp()));
                usageData.put("userName", us.getResponsible().getDisplayName());
                usageData.put("approvalTime", us.getUpdatedAt() != null ? formatterDatetime.format(us.getUpdatedAt()) : "N/A");
                usageData.put("status", us.getIsApproved() ? "Aprovado" : (us.getIsApproved() == null ? "Pendente" : "Reprovado"));
                usageDataList.add(usageData);
            }

            JRBeanCollectionDataSource usageDataSource = new JRBeanCollectionDataSource(usageDataList);

            // Passar o sub-relatório compilado como parâmetro
            parameters.put("subReportUsageApprovalRejectSubReport", usageApprovalRejectSubReport);
            parameters.put("usageApprovalsRejectionsDataSource", usageDataSource);

            // Preparar dados para o sub-relatório de uso de medicamentos
            List<Map<String, Object>> medicationUsageData = generateMedicationUsageData(user, startDate, endDate);
            JRBeanCollectionDataSource medicationUsageDataSource = new JRBeanCollectionDataSource(medicationUsageData);
            parameters.put("medicationUsageDataSource", medicationUsageDataSource);
            parameters.put("medicationUsageDataSubReport", medicationUsageSubReport);

            if (medicationUsageData.isEmpty() && usageDataList.isEmpty() && stockDataList.isEmpty()) {
                throw new ResourceNotFoundException("No data found on user");
            }

            // Preencher o relatório com dados
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // Exportar para PDF
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (DateTimeException e) {
            throw new DateTimeException("Wrong year or month value");
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStream getUserImage(String avatarUrl) {
        if (avatarUrl == null) {
            return getClass().getResourceAsStream(defaultImagePath);
        }
        try {
            // Tenta carregar a imagem do usuário a partir da URL
            URL url = new URL(avatarUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return connection.getInputStream();
            }
            // Se a URL do usuário for nula ou não estiver disponível, retorna a imagem padrão
            return getClass().getResourceAsStream(defaultImagePath);

        } catch (Exception e) {
            e.printStackTrace();
            // Em caso de erro, retorna a imagem padrão
            return getClass().getResourceAsStream(defaultImagePath);
        }
    }

    public ByteArrayInputStream generateResponsibleReport(User responsible, int year, int month) {
        try {
            OffsetDateTime startDate = OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            OffsetDateTime endDate = (startDate.getMonth() == OffsetDateTime.now(ZoneOffset.UTC).getMonth() && startDate.getYear() == OffsetDateTime.now(ZoneOffset.UTC).getYear()) ? OffsetDateTime.now(ZoneOffset.UTC) : startDate.plusMonths(1).minusSeconds(1);

            if (startDate.isAfter(OffsetDateTime.now()) || endDate.isAfter(OffsetDateTime.now())) {
                throw new ResourceNotFoundException("No data found on responsible.");
            }

            // Carregar templates JasperReports
            InputStream mainReportInput = getClass().getResourceAsStream("/templates/reports/responsible/ResponsibleReportTemplate.jrxml");
            JasperReport mainReport = JasperCompileManager.compileReport(mainReportInput);

            InputStream usageApprovalRejectSubReportInput = getClass().getResourceAsStream("/templates/reports/UsageApprovalsRejectionsSubReport.jrxml");
            JasperReport usageApprovalRejectSubReport = JasperCompileManager.compileReport(usageApprovalRejectSubReportInput);

            InputStream supervisedUserSubReportInput = getClass().getResourceAsStream("/templates/reports/responsible/SupervisedUserSubReport.jrxml");
            JasperReport supervisedUserSubReport = JasperCompileManager.compileReport(supervisedUserSubReportInput);

            InputStream medicationUsageSubReportInput = getClass().getResourceAsStream("/templates/reports/responsible/MedicationUsageSubReport.jrxml");
            JasperReport medicationUsageSubReport = JasperCompileManager.compileReport(medicationUsageSubReportInput);

            // Parâmetros básicos do responsável
            SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
            DateTimeFormatter formatterDatetime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String todayDatetime = formatterDatetime.format(OffsetDateTime.now(ZoneOffset.UTC));

            String birthDate = (responsible.getBirthDate() != null) ? formatterDate.format(responsible.getBirthDate()) : "N/A";
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("responsibleImage", getUserImage(responsible.getAvatarUrl()));
            parameters.put("displayName", responsible.getDisplayName());
            parameters.put("email", responsible.getEmail());
            parameters.put("birthDate", birthDate);
            parameters.put("todayDatetime", todayDatetime);

            // Tabela de aprovações/reprovações
            List<Usage> approvals = usageRepository.findByResponsibleIdAndActionTmstampBetween(responsible.getId(), startDate, endDate);
            List<Map<String, Object>> approvalData = prepareApprovalData(approvals);

            JRBeanCollectionDataSource usageDataSource = new JRBeanCollectionDataSource(approvalData);
            parameters.put("subReportUsageApprovalRejectSubReport", usageApprovalRejectSubReport);
            parameters.put("usageApprovalsRejectionsDataSource", usageDataSource);

            // Tabelas para cada usuário supervisionado
            List<UserResponsible> supervisedUsers = userResponsibleRepository.findByResponsibleId(responsible.getId());
            List<Map<String, Object>> supervisedUserDataList = prepareSupervisedUserData(supervisedUsers, startDate, endDate);

            parameters.put("supervisedUserSubReport", supervisedUserSubReport);
            parameters.put("supervisedUserDataSource", new JRBeanCollectionDataSource(supervisedUserDataList));

            parameters.put("medicationUsageSubReport", medicationUsageSubReport);

            if (supervisedUserDataList.isEmpty() && approvalData.isEmpty()) {
                throw new ResourceNotFoundException("No data found on responsible");
            }

            JasperPrint jasperPrint = JasperFillManager.fillReport(mainReport, parameters, new JREmptyDataSource());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (DateTimeException e) {
            throw new DateTimeException("Wrong year or month value");
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Map<String, Object>> generateMedicationUsageData(User user, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Map<String, Object>> medicationUsageData = new ArrayList<>();
        List<UserMedication> userMedications = userMedicationRepository.findByUserIdAndFirstDosageTimeBetween(user.getId(), OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), endDate);
        List<Usage> usages = usageRepository.findByUserIdAndActionTmstampBetween(user.getId(), startDate, endDate);
        Set<UUID> associatedUsageIds = new HashSet<>(); // Armazena os IDs de usos associados

        for (UserMedication userMedication : userMedications) {
            List<Map<String, Object>> doses = calculateDoseTimes(userMedication, startDate, endDate);

            for (Map<String, Object> dose : doses) {
                OffsetDateTime doseTime = (OffsetDateTime) dose.get("datetime");
                UUID medicationId = (UUID) dose.get("medicationId");
                boolean isTaken = false;
                String statusVerification = "Pendente";
                dose.put("usageTime", "N/A");

                for (Usage usage : usages) {
                    OffsetDateTime usageTime = usage.getActionTmstamp();
                    int maxTakingTime = userMedication.getMaxTakingTime() == 0.5 ? 30 : 60;

                    if (
                            usage.getUserMedicationStockUsages().get(0).getUserMedicationStock().getUserMedication().getMedication().getId() == medicationId &&
                                    Math.abs(Duration.between(doseTime, usageTime).toMinutes()) <= maxTakingTime) {
                        dose.put("usageTime", usageTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                        isTaken = true;
                        statusVerification = usage.getIsApproved() != null ? (usage.getIsApproved() ? "Aprovado" : "Reprovado") : "Pendente";
                        associatedUsageIds.add(usage.getId()); // Marca o uso como associado
                        break;
                    }
                }

                dose.put("isTaken", isTaken ? "Tomado" : "Esquecido");
                dose.put("statusVerification", isTaken ? statusVerification : "N/A");
                dose.put("sortDatetime", doseTime); // Adiciona o campo para ordenação
                medicationUsageData.add(dose);
            }
        }

        // Adicionar usos que não foram associados a nenhuma dose
        for (Usage usage : usages) {
            if (!associatedUsageIds.contains(usage.getId())) {
                Map<String, Object> extraUsage = new HashMap<>();
                extraUsage.put("medicationName", usage.getUserMedicationStockUsages().get(0).getUserMedicationStock().getUserMedication().getMedication().getName());
                extraUsage.put("expectedTime", "N/A"); // Sem horário esperado
                extraUsage.put("usageTime", usage.getActionTmstamp().format(DateTimeFormatter.ofPattern("HH:mm")));
                extraUsage.put("isTaken", "Extra"); // Marcado como Extra
                extraUsage.put("statusVerification", usage.getIsApproved() != null ? (usage.getIsApproved() ? "Aprovado" : "Reprovado") : "Pendente");
                extraUsage.put("sortDatetime", usage.getActionTmstamp()); // Usar actionTmstamp para ordenação

                medicationUsageData.add(extraUsage);
            }
        }

        // Ordena a lista final com base no campo "sortDatetime"
        medicationUsageData.sort(Comparator.comparing(entry -> (OffsetDateTime) entry.get("sortDatetime")));

        return medicationUsageData;
    }

    private List<Map<String, Object>> calculateDoseTimes(UserMedication userMedication, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Map<String, Object>> doseTimes = new ArrayList<>();
        OffsetDateTime firstDoseTime = userMedication.getFirstDosageTime();
        int interval = (int) userMedication.getTimeBetween();

        OffsetDateTime doseTime = getInitialDoseTime(startDate, firstDoseTime, (int) userMedication.getTimeBetween());
        while (doseTime.isBefore(endDate)) {
            if (wasActiveAtTheTime(userMedication.getUserMedicationStatuses(), doseTime)) {

                Map<String, Object> dose = new HashMap<>();
                dose.put("medicationId", userMedication.getMedication().getId());
                dose.put("medicationName", userMedication.getMedication().getName());
                dose.put("datetime", doseTime);
                dose.put("expectedTime", doseTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
                dose.put("usageTime", ""); // Inicialmente vazio, será preenchido se o medicamento foi tomado

                doseTimes.add(dose);
            doseTime = doseTime.plusHours(interval);
            }
        }

        return doseTimes;
    }

    private static boolean wasActiveAtTheTime(List<UserMedicationStatus> statuses, OffsetDateTime targetDate) {
        return statuses.stream()
                .sorted(Comparator.comparing(UserMedicationStatus::getCreatedAt)) // Ordena antes de filtrar
                .filter(status -> !status.getCreatedAt().isAfter(targetDate))
                .max(Comparator.comparing(UserMedicationStatus::getCreatedAt)) // Encontra o mais próximo anterior
                .map(UserMedicationStatus::isActive)
                .orElse(false);
    }

    private OffsetDateTime getInitialDoseTime(OffsetDateTime startDate, OffsetDateTime firstDoseTime, int interval) {
        if (firstDoseTime.getMonth() == OffsetDateTime.now().getMonth()) {
            return firstDoseTime;
        }
        OffsetDateTime absoluteFirstDoseTime = OffsetDateTime.of(startDate.getYear(), startDate.getMonthValue(), 1, firstDoseTime.getHour(), firstDoseTime.getMinute(), firstDoseTime.getSecond(), firstDoseTime.getNano(), ZoneOffset.UTC);

        // Calcula o número de horas a serem removidas para que o horário caia no intervalo correto (por exemplo, 00:10)
        int hoursToBeRemoved = (int) Math.floor((double) absoluteFirstDoseTime.getHour() / interval) * interval;
        OffsetDateTime ajustedFirstDoseTime = absoluteFirstDoseTime.minusHours(hoursToBeRemoved);

        return ajustedFirstDoseTime;
    }

    private List<Map<String, Object>> prepareApprovalData(List<Usage> approvals) {
        List<Map<String, Object>> approvalDataList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (Usage approval : approvals) {
            Map<String, Object> approvalData = new HashMap<>();
            approvalData.put("medicationName", approval.getUserMedicationStockUsages().get(0).getUserMedicationStock().getUserMedication().getMedication().getName());
            approvalData.put("ingestionTime", formatter.format(approval.getActionTmstamp()));
            approvalData.put("userName", approval.getUser().getDisplayName());
            approvalData.put("approvalTime", approval.getUpdatedAt() != null ? formatter.format(approval.getUpdatedAt()) : "N/A");
            approvalData.put("status", approval.getIsApproved() != null ? (approval.getIsApproved() ? "Aprovado" : "Reprovado") : "Pendente");

            approvalDataList.add(approvalData);
        }

        return approvalDataList;
    }

    private List<Map<String, Object>> prepareSupervisedUserData(List<UserResponsible> supervisedUsers, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Map<String, Object>> supervisedUserDataList = new ArrayList<>();
        for (UserResponsible userResp : supervisedUsers) {
            User supervisedUser = userResp.getUser();

            // Dados do usuário supervisionado (Cabeçalho para identificação no relatório)
            Map<String, Object> userHeaderData = new HashMap<>();
            userHeaderData.put("supervisedUserName", supervisedUser.getDisplayName());
            supervisedUserDataList.add(userHeaderData);

            // Tabela de Uso de Medicamentos do usuário supervisionado
            List<Map<String, Object>> usageDataList = generateMedicationUsageData(supervisedUser, startDate, endDate);
            userHeaderData.put("medicationUsageDataSource", new JRBeanCollectionDataSource(usageDataList));

            // Adicionar dados completos deste usuário supervisionado na lista principal
            supervisedUserDataList.add(userHeaderData);
        }

        return supervisedUserDataList;
    }

}
