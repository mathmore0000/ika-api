package ika.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

// File Entity
@Entity
@Data
@Table(name = "files", schema = "storage")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "id_bucket", nullable = false)
    private Bucket bucket;
}
