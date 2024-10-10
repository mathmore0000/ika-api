package ika.repositories;

import ika.entities.UserResponsible;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserResponsibleRepository extends JpaRepository<UserResponsible, UUID> {
    boolean existsByUserIdAndResponsibleId(UUID userId, UUID responsibleId);

    @Query("SELECT ur FROM UserResponsible ur WHERE ur.userId = :userId AND ur.responsibleId = :responsibleId AND ur.accepted = false")
    Optional<UserResponsible> findByUserIdAndResponsibleIdAndAcceptedFalse(UUID userId, UUID responsibleId);

    Optional<UserResponsible> findByUserIdAndResponsibleId(UUID userId, UUID responsibleId);

    Page<UserResponsible> findByUserId(UUID userId, Pageable pageable);

    Page<UserResponsible> findByResponsibleId(UUID responsibleId, Pageable pageable);
}
