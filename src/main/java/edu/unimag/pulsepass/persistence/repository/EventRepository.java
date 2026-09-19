package edu.unimag.pulsepass.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.unimag.pulsepass.persistence.domain.Event;
import edu.unimag.pulsepass.persistence.domain.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = "venue") //AC-002: "se recupera el evento y su venue"
    Optional<Event> findByEventCode(String eventCode); 

    // FR-EVT-005
    List<Event> findByStatusOrderByEventDateAsc(EventStatus status); 

    // FR-VEN-004: Spring navega la relación: venue.code
    List<Event> findByVenueCode(String venueCode); 

}
