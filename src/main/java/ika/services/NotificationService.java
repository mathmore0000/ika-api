package ika.services;

import ika.entities.NotificationEntity;
import ika.entities.User;
import ika.entities.aux_classes.notifications.NotificationResponse;
import ika.repositories.NotificationRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.json.JSONObject;

@Service
public class NotificationService {
    private static final String EXPO_PUSH_ENDPOINT = "https://exp.host/--/api/v2/push/send";

    @Autowired
    private NotificationRepository notificationRepository;

    public Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::convertToNotificationWithNotificationResponse);
    }

    @Transactional
    public void markAsSeen(UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setSeen(true);
        notification.setSeenAt(OffsetDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public NotificationEntity createNotification(User user, String message, String detailedMessage) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setDetailedMessage(detailedMessage);
        notification.setCreatedAt(OffsetDateTime.now());
        notification.setSeen(false); // Notificação começa como não lida
        if (user.getNotificationToken() != null){
            JSONObject body = new JSONObject(detailedMessage);
            pushNotification(user.getNotificationToken(), message, (String) body.get("message"));
        }
        return notificationRepository.save(notification);
    }

    private NotificationResponse convertToNotificationWithNotificationResponse(NotificationEntity notification) {
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId(notification.getId());
        notificationResponse.setMessage(notification.getMessage());
        notificationResponse.setDetailedMessage(notification.getDetailedMessage());
        notificationResponse.setSeen(notification.isSeen());
        notificationResponse.setSeenAt(notification.getSeenAt());
        notificationResponse.setCreatedAt(notification.getCreatedAt());

        return notificationResponse;
    }

    private void pushNotification(String expoPushToken, String title, String body) {
        if (!expoPushToken.startsWith("ExponentPushToken")) {
            return;
        }

        try {
            // Configuração do cabeçalho HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/json");
            headers.add("Content-Type", "application/json");

            // Configuração do corpo da requisição
            JSONObject notification = new JSONObject();
            notification.put("to", expoPushToken);
            notification.put("title", title);
            notification.put("body", body);
            notification.put("sound", "default");

            // Envia a notificação via POST
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<>(notification.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(EXPO_PUSH_ENDPOINT, HttpMethod.POST, request, String.class);

            // Exibe o resultado
            System.out.println("Notificação enviada: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação Expo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

