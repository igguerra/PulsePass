package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.Event;
import edu.unimag.pulsepass.persistence.domain.EventCategory;
import edu.unimag.pulsepass.persistence.domain.EventStatus;
import edu.unimag.pulsepass.persistence.domain.Venue;
import edu.unimag.pulsepass.persistence.repository.EventRepository;
import edu.unimag.pulsepass.persistence.repository.VenueRepository;

@SuppressWarnings("null")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EventRepositoryTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    VenueRepository venueRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private Venue savedVenue(String code) {
        return venueRepository.saveAndFlush(
                new Venue(code, "Venue " + code, "Santa Marta", "Carrera 1 # 1-1", 5000));
    }

    private Event newEvent(String eventCode, EventStatus status, LocalDateTime date, Venue venue) {
        return new Event(eventCode, "Evento " + eventCode, EventCategory.MUSIC, status, date, venue);
    }

    @Test
    void retrievesEventWithItsVenue() { // FR-EVT-001, AC-002
        Venue venue = savedVenue("VEN-SMR-01");
        eventRepository.saveAndFlush(newEvent("CMF-2026", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 5, 20, 0), venue));

        Event found = eventRepository.findByEventCode("CMF-2026").orElseThrow();

        assertThat(found.getVenue().getCode()).isEqualTo("VEN-SMR-01");
    }

    @Test
    void rejectsDuplicateEventCode() { // FR-EVT-002
        Venue venue = savedVenue("VEN-1");
        LocalDateTime date = LocalDateTime.of(2026, 12, 5, 20, 0);
        eventRepository.saveAndFlush(newEvent("EVT-DUP", EventStatus.DRAFT, date, venue));

        assertThatThrownBy(() ->
                eventRepository.saveAndFlush(newEvent("EVT-DUP", EventStatus.DRAFT, date, venue)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void storesCategoryAndStatusAsReadableNames() { // FR-EVT-003, FR-EVT-004, BR-008
        Venue venue = savedVenue("VEN-1");
        eventRepository.saveAndFlush(newEvent("CMF-2026", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 5, 20, 0), venue));

        String category = jdbcTemplate.queryForObject(
                "SELECT category FROM events WHERE event_code = ?", String.class, "CMF-2026");
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM events WHERE event_code = ?", String.class, "CMF-2026");

        assertThat(category).isEqualTo("MUSIC");
        assertThat(status).isEqualTo("PUBLISHED");
    }

    @Test
    void returnsOnlyPublishedEventsOrderedByDate() { // FR-EVT-005, AC-006
        Venue venue = savedVenue("VEN-1");
        eventRepository.saveAndFlush(newEvent("LATE", EventStatus.PUBLISHED,
                LocalDateTime.of(2027, 3, 1, 20, 0), venue));
        eventRepository.saveAndFlush(newEvent("EARLY", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 1, 20, 0), venue));
        eventRepository.saveAndFlush(newEvent("DRAFT-ONE", EventStatus.DRAFT,
                LocalDateTime.of(2026, 10, 1, 20, 0), venue));
        eventRepository.saveAndFlush(newEvent("CANCELLED-ONE", EventStatus.CANCELLED,
                LocalDateTime.of(2026, 9, 1, 20, 0), venue));

        List<Event> published = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);

        assertThat(published).extracting(Event::getEventCode)
                .containsExactly("EARLY", "LATE");
    }

    @Test
    void findsEventsByVenueCode() { // FR-VEN-004
        Venue venueA = savedVenue("VEN-A");
        Venue venueB = savedVenue("VEN-B");
        LocalDateTime date = LocalDateTime.of(2026, 12, 5, 20, 0);
        eventRepository.saveAndFlush(newEvent("EVT-A1", EventStatus.PUBLISHED, date, venueA));
        eventRepository.saveAndFlush(newEvent("EVT-A2", EventStatus.DRAFT, date, venueA));
        eventRepository.saveAndFlush(newEvent("EVT-B1", EventStatus.PUBLISHED, date, venueB));

        List<Event> events = eventRepository.findByVenueCode("VEN-A");

        assertThat(events).extracting(Event::getEventCode)
                .containsExactlyInAnyOrder("EVT-A1", "EVT-A2");
    }

    @Test
    void persistsOptionalStreamingUrl() { // FR-EVT-006
        Venue venue = savedVenue("VEN-1");
        Event event = newEvent("HYB-1", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 5, 20, 0), venue);
        event.setStreamingUrl("https://stream.example.com/hyb-1");
        eventRepository.saveAndFlush(event);

        String url = jdbcTemplate.queryForObject(
                "SELECT streaming_url FROM events WHERE event_code = ?", String.class, "HYB-1");

        assertThat(url).isEqualTo("https://stream.example.com/hyb-1");
    }
}