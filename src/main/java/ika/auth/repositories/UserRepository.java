package ika.auth.repositories;

import ika.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Consulta por email
    Optional<User> findByEmail(String email);

    // Consulta por número de telefone
    Optional<User> findByPhoneNumber(String phoneNumber);

    Boolean existsByEmail(String email);
}
