package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.Event;
import edu.unimag.pulsepass.persistence.domain.EventCategory;
import edu.unimag.pulsepass.persistence.domain.EventStatus;
import edu.unimag.pulsepass.persistence.domain.Venue;
import edu.unimag.pulsepass.persistence.repository.ArtistRepository;
import edu.unimag.pulsepass.persistence.repository.EventRepository;
import edu.unimag.pulsepass.persistence.repository.VenueRepository;

@SuppressWarnings("null")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EventSearchTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    VenueRepository venueRepository;

    private Venue venue(String code, String city) {
        return venueRepository.saveAndFlush(new Venue(code, "Venue " + code, city, "Calle 1", 1000));
    }

    private Event event(String code, EventStatus status, LocalDateTime date,
                        Venue venue, String... artistNames) {
        Event event = new Event(code, "Evento " + code, EventCategory.MUSIC, status, date, venue);
        for (String name : artistNames) {
            event.addArtist(artistRepository.findByStageName(name).orElseThrow());
        }
        return eventRepository.saveAndFlush(event);
    }

    @Test
    void findsEventsByArtistWithoutDuplicates() { // FR-SRC-001, AC-007
        Venue venue = venue("V1", "Santa Marta");
        event("E-LATE", EventStatus.PUBLISHED, LocalDateTime.of(2027, 1, 10, 20, 0),
                venue, "Solar Beat", "Neon Waves");
        event("E-EARLY", EventStatus.PUBLISHED, LocalDateTime.of(2026, 11, 10, 20, 0),
                venue, "Solar Beat");
        event("E-OTHER", EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 10, 20, 0),
                venue, "Ocean Drive");

        List<Event> result = eventRepository.findEventsByArtist("Solar Beat");

        assertThat(result).extracting(Event::getEventCode)
                .containsExactly("E-EARLY", "E-LATE");
    }

    @Test
    void findsEventsByCityAndArtist() { // FR-SRC-002
        Venue santaMarta = venue("V-SMR", "Santa Marta");
        Venue bogota = venue("V-BOG", "Bogotá");
        LocalDateTime date = LocalDateTime.of(2026, 12, 5, 20, 0);
        event("E-SMR", EventStatus.PUBLISHED, date, santaMarta, "Solar Beat");
        event("E-BOG", EventStatus.PUBLISHED, date, bogota, "Solar Beat");
        event("E-SMR-OTHER", EventStatus.PUBLISHED, date, santaMarta, "Ocean Drive");

        List<Event> result = eventRepository.findEventsByCityAndArtist("Santa Marta", "Solar Beat");

        assertThat(result).extracting(Event::getEventCode).containsExactly("E-SMR");
    }

    @Test
    void recommendedEventsAreFilteredCaseInsensitiveDistinctAndOrdered() { // FR-SRC-003
        Venue santaMarta = venue("V-SMR", "Santa Marta");
        Venue bogota = venue("V-BOG", "Bogotá");
        LocalDateTime from = LocalDateTime.of(2026, 10, 1, 0, 0);

        // Cumple todo; tiene dos artistas que contienen "o" -> DISTINCT evita repetirlo
        event("OK-LATE", EventStatus.PUBLISHED, LocalDateTime.of(2027, 2, 1, 20, 0),
                santaMarta, "Ocean Drive", "Solar Beat");
        event("OK-EARLY", EventStatus.PUBLISHED, LocalDateTime.of(2026, 11, 1, 20, 0),
                santaMarta, "Neon Waves");
        // Casos que NO deben aparecer
        event("DRAFT", EventStatus.DRAFT, LocalDateTime.of(2026, 12, 1, 20, 0),
                santaMarta, "Solar Beat");
        event("PAST", EventStatus.PUBLISHED, LocalDateTime.of(2026, 9, 1, 20, 0),
                santaMarta, "Solar Beat");
        event("OTHER-CITY", EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 1, 20, 0),
                bogota, "Solar Beat");
        event("NO-MATCH", EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 1, 20, 0),
                santaMarta, "Digital Pulse");

        List<Event> result = eventRepository.findRecommendedEvents(from, "Santa Marta", "O");

        assertThat(result).extracting(Event::getEventCode)
                .containsExactly("OK-EARLY", "OK-LATE");
    }
}