package ika.repositories;

import ika.entities.Usage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UsageRepository extends JpaRepository<Usage, UUID> {

    @Query("SELECT u FROM Usage u WHERE " +
            "(:isApproved IS NULL OR u.isApproved = :isApproved) AND " +
            "(cast(:fromDate as timestamp) IS NULL OR u.actionTmstamp >= :fromDate) AND " +
            "(cast(:toDate as timestamp) IS NULL OR u.actionTmstamp <= :toDate)")
    Page<Usage> findAllWithFilters(Boolean isApproved, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    Optional<Usage> findByIdAndUserId(UUID id, UUID user_id);
}
