package ika.repositories;

import ika.entities.UsageLabels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsageLabelsRepository extends JpaRepository<UsageLabels, UUID> {
    List<UsageLabels> findByUsage_Id(UUID idUsage);
    void deleteByUsageId(UUID idUsage);
}
