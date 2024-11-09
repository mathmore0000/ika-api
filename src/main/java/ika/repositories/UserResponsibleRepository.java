package ika.repositories;

import ika.entities.UserResponsible;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserResponsibleRepository extends JpaRepository<UserResponsible, UUID> {
    boolean existsByUserIdAndResponsibleId(UUID userId, UUID responsibleId);

    @Query("SELECT COUNT(ur) > 0 FROM UserResponsible ur WHERE ur.userId = :userId AND ur.responsibleId = :responsibleId AND ur.accepted = true")
    boolean existsByUserIdAndResponsibleIdAndAccepted(@Param("userId") UUID userId, @Param("responsibleId") UUID responsibleId);

    @Query("SELECT ur FROM UserResponsible ur WHERE ur.userId = :userId AND ur.responsibleId = :responsibleId AND ur.accepted = false")
    Optional<UserResponsible> findByUserIdAndResponsibleIdAndAcceptedFalse(UUID userId, UUID responsibleId);

    Optional<UserResponsible> findByUserIdAndResponsibleId(UUID userId, UUID responsibleId);

    @Query("SELECT ur FROM UserResponsible ur WHERE ur.userId = :userId AND (:accepted IS NULL OR ur.accepted = :accepted)")
    Page<UserResponsible> findByUserIdAndAccepted(UUID userId, @Param("accepted") Boolean accepted, Pageable pageable);

    @Query("SELECT ur FROM UserResponsible ur WHERE ur.responsibleId = :responsibleId AND (:accepted IS NULL OR ur.accepted = :accepted)")
    Page<UserResponsible> findByResponsibleIdAndAccepted(UUID responsibleId, @Param("accepted") Boolean accepted, Pageable pageable);

    List<UserResponsible> findByResponsibleId(UUID responsibleId);
}
