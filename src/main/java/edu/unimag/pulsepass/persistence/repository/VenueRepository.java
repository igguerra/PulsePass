package edu.unimag.pulsepass.persistence.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.unimag.pulsepass.persistence.domain.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    Optional<Venue> findByCode(String code); 

    // BR-VENUE-002: solo venues activos
    List<Venue> findByActiveTrueOrderByNameAsc();
    
}
