package edu.unimag.pulsepass.persistence.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.unimag.pulsepass.persistence.domain.Artist;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByStageName(String stageName); 

    Optional<Artist> findByStageNameIgnoreCase(String stageName);

    // BR-ARTIST-002: solo artistas activos
    List<Artist> findByActiveTrueOrderByStageNameAsc();

}
