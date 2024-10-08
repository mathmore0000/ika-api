package ika.entities;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "roles", schema = "auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    public static final String USER = "user";
    public static final String ADMIN = "admin";

    @Id
    @Column(name = "role", nullable = false, unique = true)
    private String role;
}
