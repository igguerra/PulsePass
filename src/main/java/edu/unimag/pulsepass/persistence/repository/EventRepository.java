package edu.unimag.pulsepass.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.unimag.pulsepass.persistence.domain.Event;
import edu.unimag.pulsepass.persistence.domain.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = "venue") //AC-002: "se recupera el evento y su venue"
    Optional<Event> findByEventCode(String eventCode); 

    // FR-EVT-005
    List<Event> findByStatusOrderByEventDateAsc(EventStatus status); 

    // FR-VEN-004: Spring navega la relación: venue.code
    List<Event> findByVenueCode(String venueCode); 

    // FR-SRC-001 / FR-ART-004: eventos de un artista (sin duplicados)
    @Query("""
            SELECT DISTINCT e FROM Event e
            JOIN e.artists a
            WHERE a.stageName = :stageName
            ORDER BY e.eventDate ASC
            """)
    List<Event> findEventsByArtist(@Param("stageName") String stageName);

    // FR-SRC-002: eventos de una ciudad donde participa un artista
    @Query("""
            SELECT DISTINCT e FROM Event e
            JOIN e.venue v
            JOIN e.artists a
            WHERE v.city = :city
              AND a.stageName = :stageName
            ORDER BY e.eventDate ASC
            """)
    List<Event> findEventsByCityAndArtist(@Param("city") String city,
                                          @Param("stageName") String stageName);

    // FR-SRC-003: publicados, posteriores a una fecha, en una ciudad, con artista que contenga un texto (sin distinguir mayúsculas)
    @Query("""
            SELECT DISTINCT e FROM Event e
            JOIN e.venue v
            JOIN e.artists a
            WHERE e.status = edu.unimag.pulsepass.persistence.domain.EventStatus.PUBLISHED
              AND e.eventDate > :fromDate
              AND v.city = :city
              AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistText, '%'))
            ORDER BY e.eventDate ASC
            """)
    List<Event> findRecommendedEvents(@Param("fromDate") LocalDateTime fromDate,
                                      @Param("city") String city,
                                      @Param("artistText") String artistText);

}
