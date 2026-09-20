package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
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
class TicketQueryTest {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    VenueRepository venueRepository;

    private User user(String username, String email) {
        return userRepository.saveAndFlush(new User(username, email));
    }

    private Event event(String eventCode, LocalDateTime date) {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("V-" + eventCode, "Venue " + eventCode, "Santa Marta", "Calle 1", 1000));
        return eventRepository.saveAndFlush(
                new Event(eventCode, "Evento " + eventCode, EventCategory.MUSIC,
                        EventStatus.PUBLISHED, date, venue));
    }

    private void ticket(String ticketCode, TicketStatus status, User user, Event event) {
        ticketRepository.saveAndFlush(
                new Ticket(ticketCode, TicketType.GENERAL, new BigDecimal("40.00"),
                        status, LocalDateTime.of(2026, 9, 1, 10, 0), user, event));
    }

    @Test
    void findsTicketsByUserEmailOptionallyFilteredByStatus() { // FR-TKT-006
        User user = user("andrea", "andrea@pulsepass.com");
        Event event = event("EVT-Q1", LocalDateTime.of(2026, 12, 1, 20, 0));
        ticket("TCK-A1", TicketStatus.PAID, user, event);
        ticket("TCK-A2", TicketStatus.RESERVED, user, event);

        List<Ticket> allTickets = ticketRepository.findByUserEmailIgnoreCase("ANDREA@pulsepass.com");
        List<Ticket> paidOnly = ticketRepository.findByUserEmailIgnoreCaseAndStatus(
                "andrea@pulsepass.com", TicketStatus.PAID);

        assertThat(allTickets).extracting(Ticket::getTicketCode)
                .containsExactlyInAnyOrder("TCK-A1", "TCK-A2");
        assertThat(paidOnly).extracting(Ticket::getTicketCode).containsExactly("TCK-A1");
    }

    @Test
    void findsPaidTicketsByEventCode() { // FR-TKT-007
        User user = user("carlos", "carlos@pulsepass.com");
        Event event = event("EVT-Q2", LocalDateTime.of(2026, 12, 1, 20, 0));
        ticket("TCK-B1", TicketStatus.PAID, user, event);
        ticket("TCK-B2", TicketStatus.CANCELLED, user, event);

        List<Ticket> paidTickets = ticketRepository.findByEventEventCodeAndStatus("EVT-Q2", TicketStatus.PAID);

        assertThat(paidTickets).extracting(Ticket::getTicketCode).containsExactly("TCK-B1");
    }

    @Test
    void countsOnlyPaidTicketsForAnEvent() { // FR-TKT-008, AC-008
        User user = user("laura", "laura@pulsepass.com");
        Event event = event("EVT-Q3", LocalDateTime.of(2026, 12, 1, 20, 0));
        ticket("TCK-C1", TicketStatus.PAID, user, event);
        ticket("TCK-C2", TicketStatus.PAID, user, event);
        ticket("TCK-C3", TicketStatus.RESERVED, user, event);
        ticket("TCK-C4", TicketStatus.CANCELLED, user, event);

        long paidCount = ticketRepository.countPaidTicketsByEventCode("EVT-Q3");

        assertThat(paidCount).isEqualTo(2);
    }

    @Test
    void findsTicketsForEventsAfterADateOrderedChronologically() { // FR-SRC-004
        User user = user("miguel", "miguel@pulsepass.com");
        Event pastEvent = event("EVT-PAST", LocalDateTime.of(2026, 1, 1, 20, 0));
        Event earlyEvent = event("EVT-EARLY", LocalDateTime.of(2026, 11, 1, 20, 0));
        Event lateEvent = event("EVT-LATE", LocalDateTime.of(2027, 1, 1, 20, 0));
        ticket("TCK-D1", TicketStatus.PAID, user, pastEvent);
        ticket("TCK-D2", TicketStatus.PAID, user, lateEvent);
        ticket("TCK-D3", TicketStatus.PAID, user, earlyEvent);

        List<Ticket> upcoming = ticketRepository.findTicketsForUpcomingEvents(
                LocalDateTime.of(2026, 10, 1, 0, 0));

        assertThat(upcoming).extracting(Ticket::getTicketCode)
                .containsExactly("TCK-D3", "TCK-D2");
    }
}
