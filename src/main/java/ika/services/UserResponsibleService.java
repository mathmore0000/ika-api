package ika.services;

import ika.entities.User;
import ika.entities.UserResponsible;
import ika.repositories.UserRepository;
import ika.repositories.UserResponsibleRepository;
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
    private UserRepository userRepository;

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
        return Optional.of(userResponsibleRepository.save(userResponsible));
    }

    public Optional<UserResponsible> acceptResponsibleRequest(UUID idResponsible, UUID currentUserId) {
        Optional<UserResponsible> userResponsible = userResponsibleRepository
                .findByUserIdAndResponsibleIdAndAcceptedFalse(currentUserId, idResponsible);
        if (userResponsible.isEmpty()) {
            return Optional.empty();
        }
        userResponsible.ifPresent(responsible -> {
            responsible.setAccepted(true);
            userResponsibleRepository.save(responsible);
        });

        return userResponsible;
    }

    public boolean deleteResponsibleRequest(UUID idUser, UUID idResponsible) {
        Optional<UserResponsible> userResponsible = userResponsibleRepository
                .findByUserIdAndResponsibleId(idUser, idResponsible);

        if (userResponsible.isPresent()) {
            userResponsibleRepository.delete(userResponsible.get());
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
