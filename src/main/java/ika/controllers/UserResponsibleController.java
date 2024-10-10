package ika.controllers;

import ika.controllers.aux_classes.CustomPageResponse;
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
        UUID idUser = currentUserProvider.getCurrentUserId();
        Optional<UserResponsible> createdRequest = userResponsibleService.createResponsibleRequest(idUser, idResponsible);

        return createdRequest
                .map(request -> ResponseEntity.status(HttpStatus.CREATED).body(request))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());  // Conflict if duplicate
    }

    @PutMapping("/accept")
    public ResponseEntity<UserResponsible> acceptRequest(@RequestParam UUID idResponsible) {
        UUID idUser = currentUserProvider.getCurrentUserId();
        Optional<UserResponsible> acceptedRequest = userResponsibleService.acceptResponsibleRequest(idResponsible, idUser);

        return acceptedRequest
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/by-responsible")
    public ResponseEntity<Void> deleteRequestByResponsible(@RequestParam UUID idResponsible) {
        UUID idUser = currentUserProvider.getCurrentUserId();
        boolean deleted = userResponsibleService.deleteResponsibleRequest(idUser, idResponsible);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/by-user")
    public ResponseEntity<Void> deleteRequestByUser(@RequestParam @NotNull(message = "idUser parameter is required") UUID idUser) {
        UUID idResponsible = currentUserProvider.getCurrentUserId();
        boolean deleted = userResponsibleService.deleteResponsibleRequest(idUser, idResponsible);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/responsible")
    public ResponseEntity<CustomPageResponse<UserResponsible>> getAllResponsible(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "200") int size,
            @RequestParam(value = "accepted", required = false) Boolean accepted,  // Use Boolean to allow null values
            @RequestParam(defaultValue = "createdAt") String sortBy,  // Sorting field
            @RequestParam(defaultValue = "asc") String sortDirection) { // Sorting direction
        UUID idResponsible = currentUserProvider.getCurrentUserId();
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);
        Page<UserResponsible> responsiblesPage = userResponsibleService.getAllResponsibles(idResponsible, accepted, pageable);

        CustomPageResponse<UserResponsible> customPageResponse = new CustomPageResponse<>(
                responsiblesPage.getContent(),
                responsiblesPage.getNumber(),
                responsiblesPage.getSize(),
                responsiblesPage.getSort(),
                responsiblesPage.getPageable().getOffset(),
                responsiblesPage.getTotalPages()
        );

        return ResponseEntity.ok(customPageResponse);
    }


    @GetMapping("/user")
    public ResponseEntity<CustomPageResponse<UserResponsible>> getAllUser(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "200") int size,
            @RequestParam(value = "accepted", required = false) Boolean accepted,
            @RequestParam(defaultValue = "createdAt") String sortBy,  // Campo de ordenação
            @RequestParam(defaultValue = "asc") String sortDirection) { // Direção de ordenação)
        UUID idUser = currentUserProvider.getCurrentUserId();

        page = CustomPageResponse.getValidPage(page);
        size = CustomPageResponse.getValidSize(size);

        // Cria um objeto Pageable com base nos parâmetros page e size
        Pageable pageable = CustomPageResponse.createPageableWithSort(page, size, sortBy, sortDirection);
        Page<UserResponsible> usersPage = userResponsibleService.getAllUsers(idUser, accepted, pageable);

        CustomPageResponse<UserResponsible> customPageResponse = new CustomPageResponse<>(
                usersPage.getContent(),
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getSort(),
                usersPage.getPageable().getOffset(),
                usersPage.getTotalPages()
        );
        return ResponseEntity.ok(customPageResponse);
    }
}
