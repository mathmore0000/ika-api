package ika.auth.entities;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "locale")
    private String locale;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role", nullable = false)
    private Role role;

    @Column(name = "disabled", nullable = false)
    private boolean disabled;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "avatar_url")
    private String avatarUrl;
}
