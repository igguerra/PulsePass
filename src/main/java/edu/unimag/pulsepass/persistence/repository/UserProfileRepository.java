package edu.unimag.pulsepass.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.unimag.pulsepass.persistence.domain.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // FR-USR-004: recuperar el perfil navegando la relación 1:1 desde el user
    Optional<UserProfile> findByUserId(Long userId);

}
