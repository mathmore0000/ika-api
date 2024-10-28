package ika.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@IdClass(UserResponsible.UserResponsibleId.class) // Define a chave composta
@Table(name = "user_responsibles", schema = "public")
public class UserResponsible {

    @Id
    @Column(name = "id_user", insertable = false, updatable = false) // Mapeia como parte da chave composta, mas deixa a coluna gerenciada por `@ManyToOne`
    private UUID userId;

    @Id
    @Column(name = "id_responsible", insertable = false, updatable = false) // Mapeia como parte da chave composta
    private UUID responsibleId;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id") // Vincula `user` ao ID `userId`
    @MapsId("userId") // Mapeia `userId` como parte da chave composta
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_responsible", referencedColumnName = "id") // Vincula `responsible` ao ID `responsibleId`
    @MapsId("responsibleId") // Mapeia `responsibleId` como parte da chave composta
    private User responsible;

    @Column(name = "accepted", nullable = false)
    private Boolean accepted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Construtor para inicializar `user` e `responsible`
    public UserResponsible(User user, User responsible) {
        this.user = user;
        this.responsible = responsible;
        this.userId = user.getId(); // Inicializa `userId` com o ID do usuário
        this.responsibleId = responsible.getId(); // Inicializa `responsibleId` com o ID do responsável
        this.createdAt = LocalDateTime.now();
    }

    // Classe interna representando a chave composta
    @Data
    @NoArgsConstructor
    public static class UserResponsibleId implements Serializable {
        private UUID userId;
        private UUID responsibleId;

        public UserResponsibleId(UUID userId, UUID responsibleId) {
            this.userId = userId;
            this.responsibleId = responsibleId;
        }
    }
}
