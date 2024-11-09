package ika.services;

import ika.entities.Notification;
import ika.entities.User;
import ika.entities.aux_classes.notifications.NotificationResponse;
import ika.repositories.NotificationRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtAsc(userId, pageable).map(this::convertToNotificationWithNotificationResponse);
    }

    @Transactional
    public void markAsSeen(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setSeen(true);
        notification.setSeenAt(OffsetDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public Notification createNotification(User user, String message, String detailedMessage) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setDetailedMessage(detailedMessage);
        notification.setCreatedAt(OffsetDateTime.now());
        notification.setSeen(false); // Notificação começa como não lida
        return notificationRepository.save(notification);
    }

    private NotificationResponse convertToNotificationWithNotificationResponse(Notification notification){
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId(notification.getId());
        notificationResponse.setMessage(notification.getMessage());
        notificationResponse.setDetailedMessage(notification.getDetailedMessage());
        notificationResponse.setSeen(notification.isSeen());
        notificationResponse.setSeenAt(notification.getSeenAt());
        notificationResponse.setCreatedAt(notification.getCreatedAt());

        return notificationResponse;
    }
}

