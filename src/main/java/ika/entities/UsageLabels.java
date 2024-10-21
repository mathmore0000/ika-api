package ika.entities;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "usage_labels", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageLabels {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_label", nullable = false)
    private Label label;

    @ManyToOne
    @JoinColumn(name = "id_usage", nullable = false)
    private Usage usage;
}
