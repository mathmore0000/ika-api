package ika.controllers;

import ika.entities.UserResponsible;
import ika.services.UserResponsibleService;
import ika.utils.CurrentUserProvider;  // Assuming you have this utility
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/responsibles")
public class UserResponsibleController {

    @Autowired
    private UserResponsibleService userResponsibleService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @PostMapping()
    public ResponseEntity<UserResponsible> createRequest(@RequestParam UUID idResponsible) {
        System.out.println(idResponsible);
        UUID idUser = currentUserProvider.getCurrentUser().getId();
        Optional<UserResponsible> createdRequest = userResponsibleService.createResponsibleRequest(idUser, idResponsible);

        return createdRequest
                .map(request -> ResponseEntity.status(HttpStatus.CREATED).body(request))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());  // Conflict if duplicate
    }

    @PutMapping("/accept")
    public ResponseEntity<UserResponsible> acceptRequest(@RequestParam UUID idResponsible) {
        UUID idUser = currentUserProvider.getCurrentUser().getId();
        Optional<UserResponsible> acceptedRequest = userResponsibleService.acceptResponsibleRequest(idResponsible, idUser);

        return acceptedRequest
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/by-responsible")
    public ResponseEntity<Void> deleteRequestByResponsible(@RequestParam UUID idResponsible) {
        UUID idUser = currentUserProvider.getCurrentUser().getId();
        boolean deleted = userResponsibleService.deleteResponsibleRequest(idUser, idResponsible);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/by-user")
    public ResponseEntity<Void> deleteRequestByUser(@RequestParam @NotNull(message = "idUser parameter is required") UUID idUser) {
        UUID idResponsible = currentUserProvider.getCurrentUser().getId();
        boolean deleted = userResponsibleService.deleteResponsibleRequest(idUser, idResponsible);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/responsible")
    public ResponseEntity<Page<UserResponsible>> getAllResponsible(Pageable pageable) {
        UUID idResponsible = currentUserProvider.getCurrentUser().getId();
        Page<UserResponsible> responsibles = userResponsibleService.getAllResponsibles(idResponsible, pageable);
        return ResponseEntity.ok(responsibles);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<UserResponsible>> getAllUser(Pageable pageable) {
        UUID idUser = currentUserProvider.getCurrentUser().getId();
        Page<UserResponsible> users = userResponsibleService.getAllUsers(idUser, pageable);
        return ResponseEntity.ok(users);
    }
}
