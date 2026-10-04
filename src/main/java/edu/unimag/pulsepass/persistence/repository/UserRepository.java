package edu.unimag.pulsepass.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.unimag.pulsepass.persistence.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // FR-USR / búsqueda por email ignorando mayúsculas
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    // BR-USER-002: email único ignorando mayúsculas/minúsculas
    boolean existsByEmailIgnoreCase(String email);

    // BR-USER-001: username único
    boolean existsByUsername(String username);

}
