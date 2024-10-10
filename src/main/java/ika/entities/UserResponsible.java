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
@IdClass(UserResponsible.UserResponsibleId.class) // Define the composite key directly in the entity
@Table(name = "user_responsibles", schema = "public")
public class UserResponsible {

    @Id
    @Column(name = "id_user", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "id_responsible", nullable = false)
    private UUID responsibleId;

    @Column(name = "accepted", nullable = false)
    private Boolean accepted = false;

    @Column(name = "datetime", nullable = false)
    private LocalDateTime datetime = LocalDateTime.now();

    // Constructor to set userId and responsibleId
    public UserResponsible(UUID userId, UUID responsibleId) {
        this.userId = userId;
        this.responsibleId = responsibleId;
        this.datetime = LocalDateTime.now();
    }

    // Inner class representing the composite key
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
