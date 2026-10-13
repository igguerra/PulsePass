package edu.unimag.pulsepass.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.unimag.pulsepass.persistence.domain.UserCredential;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    // Login: el identificador puede ser username (exacto) o email (sin distinguir mayúsculas)
    @EntityGraph(attributePaths = {"user", "roles"})
    @Query("""
            SELECT c FROM UserCredential c
            WHERE c.user.username = :identifier
                OR LOWER(c.user.email) = LOWER(:identifier)
            """)
    Optional<UserCredential> findByIdentifier(@Param("identifier") String identifier); 

    // Filtro JWT: el claim "sub" es el ID del usuario; se recarga estado y roles en cada request
    @EntityGraph(attributePaths = {"user", "roles"})
    Optional<UserCredential> findByUserId(Long userId); 

}
