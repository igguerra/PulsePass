package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.Artist;
import edu.unimag.pulsepass.persistence.domain.Event;
import edu.unimag.pulsepass.persistence.domain.EventCategory;
import edu.unimag.pulsepass.persistence.domain.EventStatus;
import edu.unimag.pulsepass.persistence.domain.Venue;
import edu.unimag.pulsepass.persistence.repository.ArtistRepository;
import edu.unimag.pulsepass.persistence.repository.EventRepository;
import edu.unimag.pulsepass.persistence.repository.VenueRepository;
import jakarta.persistence.EntityManager;

@SuppressWarnings("null")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EventArtistTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    VenueRepository venueRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EntityManager entityManager;

    private Event savedEvent(String eventCode) {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-" + eventCode, "Venue", "Santa Marta", "Calle 1", 5000));
        return eventRepository.saveAndFlush(new Event(eventCode, "Evento " + eventCode,
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 5, 20, 0), venue));
    }

    private Artist seeded(String stageName) {
        return artistRepository.findByStageName(stageName).orElseThrow();
    }

    @Test
    void associatesThreeArtistsWithAnEvent() { // FR-ART-003, AC-003
        Event event = savedEvent("CMF-2026");
        event.addArtist(seeded("Solar Beat"));
        event.addArtist(seeded("Neon Waves"));
        event.addArtist(seeded("Caribbean Sound"));
        eventRepository.saveAndFlush(event);

        // Vacía el contexto para leer lo que realmente quedó en la base
        entityManager.clear();

        Event reloaded = eventRepository.findByEventCode("CMF-2026").orElseThrow();
        assertThat(reloaded.getArtists()).extracting(Artist::getStageName)
                .containsExactlyInAnyOrder("Solar Beat", "Neon Waves", "Caribbean Sound");

        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM event_artists WHERE event_id = ?",
                Integer.class, reloaded.getId());
        assertThat(rows).isEqualTo(3);
    }

    @Test
    void doesNotDuplicateTheSameArtistInAnEvent() { // FR-ART-003
        Event event = savedEvent("DUP-1");
        Artist solarBeat = seeded("Solar Beat");
        event.addArtist(solarBeat);
        event.addArtist(solarBeat);
        eventRepository.saveAndFlush(event);

        assertThat(event.getArtists()).hasSize(1);
        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM event_artists WHERE event_id = ?",
                Integer.class, event.getId());
        assertThat(rows).isEqualTo(1);
    }

    @Test
    void databaseRejectsDuplicatePairEvenBypassingJpa() { // PK compuesta
        Event event = savedEvent("PK-1");
        Artist solarBeat = seeded("Solar Beat");
        event.addArtist(solarBeat);
        eventRepository.saveAndFlush(event);

        Long eventId = event.getId();
        Long artistId = solarBeat.getId();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO event_artists (event_id, artist_id) VALUES (?, ?)",
                eventId, artistId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void anArtistCanBelongToSeveralEvents() { // BR-003
        Event first = savedEvent("EVT-1");
        Event second = savedEvent("EVT-2");
        Artist solarBeat = seeded("Solar Beat");
        first.addArtist(solarBeat);
        second.addArtist(solarBeat);
        eventRepository.saveAndFlush(first);
        eventRepository.saveAndFlush(second);

        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM event_artists WHERE artist_id = ?",
                Integer.class, solarBeat.getId());
        assertThat(rows).isEqualTo(2);
    }
}