package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.Event;
import edu.unimag.pulsepass.persistence.domain.EventCategory;
import edu.unimag.pulsepass.persistence.domain.EventStatus;
import edu.unimag.pulsepass.persistence.domain.Ticket;
import edu.unimag.pulsepass.persistence.domain.TicketStatus;
import edu.unimag.pulsepass.persistence.domain.TicketType;
import edu.unimag.pulsepass.persistence.domain.User;
import edu.unimag.pulsepass.persistence.domain.Venue;
import edu.unimag.pulsepass.persistence.repository.EventRepository;
import edu.unimag.pulsepass.persistence.repository.TicketRepository;
import edu.unimag.pulsepass.persistence.repository.UserRepository;
import edu.unimag.pulsepass.persistence.repository.VenueRepository;

@SuppressWarnings("null")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class TicketRepositoryTest {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    VenueRepository venueRepository;

    private static final LocalDateTime PURCHASE_DATE = LocalDateTime.of(2026, 10, 1, 10, 0);

    private User user(String username, String email) {
        return userRepository.saveAndFlush(new User(username, email));
    }

    private Event event(String eventCode) {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("V-" + eventCode, "Venue " + eventCode, "Santa Marta", "Calle 1", 1000));
        return eventRepository.saveAndFlush(
                new Event(eventCode, "Evento " + eventCode, EventCategory.MUSIC,
                        EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 1, 20, 0), venue));
    }

    private Ticket ticket(String ticketCode, BigDecimal price, TicketStatus status, User user, Event event) {
        return new Ticket(ticketCode, TicketType.GENERAL, price, status, PURCHASE_DATE, user, event);
    }

    @Test
    void persistsAndRetrievesTicket() { // FR-TKT-001, FR-TKT-004, FR-TKT-005
        User user = user("andrea", "andrea@pulsepass.com");
        Event event = event("EVT-001");

        Ticket saved = ticketRepository.saveAndFlush(
                ticket("TCK-0001", new BigDecimal("50.00"), TicketStatus.PAID, user, event));

        Ticket found = ticketRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getType()).isEqualTo(TicketType.GENERAL);
        assertThat(found.getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(found.getPrice()).isEqualByComparingTo("50.00");
        assertThat(found.getUser().getEmail()).isEqualTo("andrea@pulsepass.com");
        assertThat(found.getEvent().getEventCode()).isEqualTo("EVT-001");
    }

    @Test
    void rejectsTicketWithoutUser() { // FR-TKT-001
        Event event = event("EVT-002");

        assertThatThrownBy(() ->
                ticketRepository.saveAndFlush(
                        ticket("TCK-0002", new BigDecimal("50.00"), TicketStatus.RESERVED, null, event)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsTicketWithoutEvent() { // FR-TKT-001
        User user = user("carlos", "carlos@pulsepass.com");

        assertThatThrownBy(() ->
                ticketRepository.saveAndFlush(
                        ticket("TCK-0003", new BigDecimal("50.00"), TicketStatus.RESERVED, user, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateTicketCode() { // FR-TKT-002, AC-005
        User user = user("miguel", "miguel@pulsepass.com");
        Event event = event("EVT-003");
        ticketRepository.saveAndFlush(
                ticket("TCK-0004", new BigDecimal("30.00"), TicketStatus.PAID, user, event));

        User otherUser = user("laura", "laura@pulsepass.com");
        assertThatThrownBy(() ->
                ticketRepository.saveAndFlush(
                        ticket("TCK-0004", new BigDecimal("30.00"), TicketStatus.PAID, otherUser, event)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsNegativePrice() { // FR-TKT-003
        User user = user("valentina", "valentina@pulsepass.com");
        Event event = event("EVT-004");

        assertThatThrownBy(() ->
                ticketRepository.saveAndFlush(
                        ticket("TCK-0005", new BigDecimal("-10.00"), TicketStatus.RESERVED, user, event)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
