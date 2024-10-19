package ika.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "usage", schema = "public")
public class Usage {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;
    @Column(name = "id_user", nullable = false)
    private UUID userId;
    @Column(name = "action_tmstamp", nullable = false)
    private LocalDateTime actionTmstamp;
    @Column(name = "is_approved")
    private Boolean isApproved;

    @OneToOne
    @JoinColumn(name = "id_file")
    private FileEntity video;
}

