package ika.services;

import ika.entities.UserResponsible;
import ika.repositories.UserRepository;
import ika.repositories.UserResponsibleRepository;
import ika.utils.exceptions.ResourceNotFoundException;
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

    public Optional<UserResponsible> createResponsibleRequest(UUID idUser, UUID idResponsible) {
        // Check for existing relationship to prevent duplicates
        if (!userRepository.existsById(idResponsible)) {
            throw new ResourceNotFoundException("Responsible not found");
        }
        if (userResponsibleRepository.existsByUserIdAndResponsibleId(idUser, idResponsible)) {
            return Optional.empty();
        }

        UserResponsible userResponsible = new UserResponsible();
        userResponsible.setUserId(idUser);
        userResponsible.setResponsibleId(idResponsible);
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
