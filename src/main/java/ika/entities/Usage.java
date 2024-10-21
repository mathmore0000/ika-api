package ika.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    @Column(name = "obs")
    private String Obs;

    @OneToOne
    @JoinColumn(name = "id_file")
    private FileEntity video;

    @ManyToMany
    @JoinTable(
            name = "usage_labels",
            joinColumns = @JoinColumn(name = "id_usage"),
            inverseJoinColumns = @JoinColumn(name = "id_label")
    )
    private Set<Label> labels = new HashSet<>();
}

