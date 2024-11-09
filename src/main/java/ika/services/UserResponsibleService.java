package ika.services;

import ika.entities.User;
import ika.entities.UserResponsible;
import ika.repositories.UserRepository;
import ika.repositories.UserResponsibleRepository;
import ika.utils.CurrentUserProvider;
import ika.utils.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserResponsibleService {

    @Autowired
    private UserResponsibleRepository userResponsibleRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Optional<UserResponsible> createResponsibleRequest(UUID userId, UUID responsibleId) {
        if (userResponsibleRepository.existsByUserIdAndResponsibleId(userId, responsibleId)) {
            return Optional.empty();
        }
        User user = entityManager.find(User.class, userId);
        User responsible = entityManager.find(User.class, responsibleId);

        UserResponsible userResponsible = new UserResponsible();
        userResponsible.setResponsible(responsible);
        userResponsible.setUser(user);

        notificationService.createNotification(userResponsible.getResponsible(), "Novo pedido de supervisionamento", "{\"message\": \"O usuário " + user.getDisplayName() + " está pedindo supervisionamento.\"}");
        return Optional.of(userResponsibleRepository.save(userResponsible));
    }

    public Optional<UserResponsible> acceptResponsibleRequest(UUID idResponsible, UUID currentUserId) {
        Optional<UserResponsible> userResponsible = userResponsibleRepository
                .findByUserIdAndResponsibleIdAndAcceptedFalseOrNull(currentUserId, idResponsible);
        if (userResponsible.isEmpty()) {
            throw new ResourceNotFoundException("No user responsible relations found");
        }
        userResponsible.ifPresent(ur -> {
            ur.setAccepted(true);
            notificationService.createNotification(ur.getUser(), "Pedido de supervisionamento aceito", "{\"message\": \"O usuário " + ur.getResponsible().getDisplayName() + " aceitou seu pedido de supervisionamento.\"}");
            userResponsibleRepository.save(ur);
        });

        return userResponsible;
    }

    public boolean deleteResponsibleRequest(UUID idUser, UUID idResponsible) {
        Optional<UserResponsible> userResponsible = userResponsibleRepository
                .findByUserIdAndResponsibleId(idUser, idResponsible);

        User currentUser = currentUserProvider.getCurrentUser();
        if (userResponsible.isPresent()) {
            userResponsibleRepository.delete(userResponsible.get());

            if (userResponsible.get().getAccepted() == null) {
                if (currentUser == userResponsible.get().getResponsible()) {
                    notificationService.createNotification(userResponsible.get().getUser(), "Pedido de supervisionamento negado", "{\"message\": \"O usuário " + userResponsible.get().getResponsible().getDisplayName() + " negou seu pedido de supervisionamento.\"}");
                }
            } else {
                if (currentUser == userResponsible.get().getUser()) {
                    notificationService.createNotification(userResponsible.get().getResponsible(), "Supervisionador excluido", "{\"message\": \"O usuário " + userResponsible.get().getUser().getDisplayName() + " excluiu seu supervisionamento.\"}");
                } else {
                    notificationService.createNotification(userResponsible.get().getUser(), "Supervisionador excluido", "{\"message\": \"O usuário " + userResponsible.get().getResponsible().getDisplayName() + " excluiu seu supervisionamento.\"}");
                }
            }
            return true;
        }

        return false;
    }

    public Page<UserResponsible> getAllResponsibles(UUID idResponsible, Boolean accepted, Pageable pageable) {
        return userResponsibleRepository.findByResponsibleIdAndAccepted(idResponsible, accepted, pageable);
    }


    public Page<UserResponsible> getAllUsers(UUID idUser, Boolean accepted, Pageable pageable) {
        return userResponsibleRepository.findByUserIdAndAccepted(idUser, accepted, pageable);
    }
}
