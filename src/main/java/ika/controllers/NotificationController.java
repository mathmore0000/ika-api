package ika.controllers;

import ika.entities.User;
import ika.entities.aux_classes.notifications.NotificationResponse;
import ika.services.NotificationService;
import ika.utils.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        User currentUser = currentUserProvider.getCurrentUser();
        Page<NotificationResponse> notifications = notificationService.getNotifications(currentUser.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/seen")
    public ResponseEntity<Void> markAsSeen(@PathVariable("id") UUID notificationId) {
        notificationService.markAsSeen(notificationId);
        return ResponseEntity.noContent().build();
    }
}

