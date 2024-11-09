package ika.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
    @Column(name = "action_tmstamp", nullable = false)
    private OffsetDateTime actionTmstamp;
    @Column(name = "updated_at", nullable = true)
    private OffsetDateTime updatedAt;
    @Column(name = "is_approved")
    private Boolean isApproved;
    @Column(name = "obs")
    private String Obs;

    @OneToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "id_file")
    private FileEntity video;

    @OneToOne
    @JoinColumn(name = "id_responsible", nullable = true)
    private User responsible;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "usage_labels",
            joinColumns = @JoinColumn(name = "id_usage"),
            inverseJoinColumns = @JoinColumn(name = "id_label")
    )
    private Set<Label> labels = new HashSet<>();

    @OneToMany(mappedBy = "usage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMedicationStockUsage> userMedicationStockUsages;  // Assuming you have a mappedBy in UserMedicationStockUsage
}

