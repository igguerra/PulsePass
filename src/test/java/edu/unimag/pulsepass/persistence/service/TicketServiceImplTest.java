package edu.unimag.pulsepass.persistence.service;

import edu.unimag.pulsepass.persistence.domain.*;
import edu.unimag.pulsepass.persistence.dto.request.PurchaseTicketRequest;
import edu.unimag.pulsepass.persistence.dto.response.TicketResponse;
import edu.unimag.pulsepass.persistence.exception.BusinessRuleException;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.mapper.TicketMapper;
import edu.unimag.pulsepass.persistence.repository.EventRepository;
import edu.unimag.pulsepass.persistence.repository.TicketRepository;
import edu.unimag.pulsepass.persistence.repository.UserProfileRepository;
import edu.unimag.pulsepass.persistence.repository.UserRepository;
import edu.unimag.pulsepass.persistence.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper mapper;

    private final TicketPriceCalculator priceCalculator = new TicketPriceCalculator();

    private TicketServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TicketServiceImpl(
                userRepository, eventRepository, userProfileRepository,
                ticketRepository, mapper, priceCalculator);
    }

    private User buildUser(boolean active) {
        User user = new User("andrea", "andrea@email.com");
        user.setActive(active);
        return user;
    }

    private Venue buildVenue(int capacity) {
        return new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Calle 1", capacity);
    }

    private Event buildEvent(EventStatus status, LocalDateTime eventDate, int minimumAge, Venue venue) {
        Event event = new Event("CMF-2026", "Caribbean Music Fest", EventCategory.MUSIC, status, eventDate, venue);
        event.setMinimumAge(minimumAge);
        return event;
    }

    private PurchaseTicketRequest buildRequest() {
        return new PurchaseTicketRequest("andrea@email.com", "CMF-2026", TicketType.GENERAL);
    }

    private void mockHappyPathSaves() {
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(
                new TicketResponse(1L, "TKT-X", TicketType.GENERAL, null, TicketStatus.PAID,
                        LocalDateTime.now(), "andrea@email.com", "CMF-2026", "Caribbean Music Fest"));
    }

    // TEST-TICKET-001: compra válida → ticket PAID
    @Test
    void deberiaRegistrarCompraValida() {
        Venue venue = buildVenue(3);
        Event event = buildEvent(EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 0, venue);
        User user = buildUser(true);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(ticketRepository.countPaidTicketsByEventCode("CMF-2026")).thenReturn(0L);
        mockHappyPathSaves();

        TicketResponse result = service.purchase(buildRequest());

        assertThat(result.status()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository).save(any(Ticket.class));
    }

    // TEST-TICKET-002: usuario inexistente → ResourceNotFoundException
    @Test
    void deberiaLanzarExcepcionCuandoUsuarioNoExiste() {
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-003: usuario inactivo → BusinessRuleException
    @Test
    void noDeberiaComprarCuandoUsuarioEstaInactivo() {
        User user = buildUser(false);
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-004: evento DRAFT → BusinessRuleException
    @Test
    void noDeberiaComprarCuandoEventoEstaEnDraft() {
        Venue venue = buildVenue(3);
        Event event = buildEvent(EventStatus.DRAFT, LocalDateTime.now().plusDays(10), 0, venue);
        User user = buildUser(true);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-005: evento CANCELLED → BusinessRuleException
    @Test
    void noDeberiaComprarCuandoEventoEstaCancelado() {
        Venue venue = buildVenue(3);
        Event event = buildEvent(EventStatus.CANCELLED, LocalDateTime.now().plusDays(10), 0, venue);
        User user = buildUser(true);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-006: usuario menor de edad → BusinessRuleException
    @Test
    void noDeberiaComprarCuandoUsuarioEsMenorDeEdad() {
        Venue venue = buildVenue(3);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(10);
        Event event = buildEvent(EventStatus.PUBLISHED, eventDate, 18, venue);
        User user = buildUser(true);

        UserProfile profile = new UserProfile("Laura", "Gómez", user);
        profile.setBirthDate(eventDate.toLocalDate().minusYears(17));

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(userProfileRepository.findByUserId(any())).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-007: evento sin capacidad → BusinessRuleException
    @Test
    void noDeberiaComprarCuandoNoHayCapacidad() {
        Venue venue = buildVenue(3);
        Event event = buildEvent(EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 0, venue);
        User user = buildUser(true);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(ticketRepository.countPaidTicketsByEventCode("CMF-2026")).thenReturn(3L);

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-008: último ticket disponible → guarda y cambia evento a SOLD_OUT
    @Test
    void deberiaMarcarEventoComoSoldOutCuandoSeCompletaCapacidad() {
        Venue venue = buildVenue(3);
        Event event = buildEvent(EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 0, venue);
        User user = buildUser(true);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(ticketRepository.countPaidTicketsByEventCode("CMF-2026")).thenReturn(2L);
        mockHappyPathSaves();

        service.purchase(buildRequest());

        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        verify(eventRepository).save(event);
    }

    // TEST-TICKET-009: cancelar ticket PAID → CANCELLED
    @Test
    void deberiaCancelarTicketPagado() {
        Venue venue = buildVenue(3);
        Event event = buildEvent(EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 0, venue);
        User user = buildUser(true);
        Ticket ticket = new Ticket("TKT-001", TicketType.GENERAL, null, TicketStatus.PAID,
                LocalDateTime.now(), user, event);

        when(ticketRepository.findByTicketCode("TKT-001")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(
                new TicketResponse(1L, "TKT-001", TicketType.GENERAL, null, TicketStatus.CANCELLED,
                        LocalDateTime.now(), "andrea@email.com", "CMF-2026", "Caribbean Music Fest"));

        TicketResponse result = service.cancel("TKT-001");

        assertThat(result.status()).isEqualTo(TicketStatus.CANCELLED);
    }

    // TEST-TICKET-010: cancelar ticket USED → BusinessRuleException
    @Test
    void noDeberiaCancelarTicketUsado() {
        Venue venue = buildVenue(3);
        Event event = buildEvent(EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 0, venue);
        User user = buildUser(true);
        Ticket ticket = new Ticket("TKT-002", TicketType.GENERAL, null, TicketStatus.USED,
                LocalDateTime.now(), user, event);

        when(ticketRepository.findByTicketCode("TKT-002")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.cancel("TKT-002"))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-011: marcar PAID como usado → USED
    @Test
    void deberiaMarcarTicketPagadoComoUsado() {
        Venue venue = buildVenue(3);
        Event event = buildEvent(EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 0, venue);
        User user = buildUser(true);
        Ticket ticket = new Ticket("TKT-003", TicketType.GENERAL, null, TicketStatus.PAID,
                LocalDateTime.now(), user, event);

        when(ticketRepository.findByTicketCode("TKT-003")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(
                new TicketResponse(1L, "TKT-003", TicketType.GENERAL, null, TicketStatus.USED,
                        LocalDateTime.now(), "andrea@email.com", "CMF-2026", "Caribbean Music Fest"));

        TicketResponse result = service.markAsUsed("TKT-003");

        assertThat(result.status()).isEqualTo(TicketStatus.USED);
    }

    // TEST-TICKET-012: usar ticket CANCELLED → BusinessRuleException
    @Test
    void noDeberiaUsarTicketCancelado() {
        Venue venue = buildVenue(3);
        Event event = buildEvent(EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 0, venue);
        User user = buildUser(true);
        Ticket ticket = new Ticket("TKT-004", TicketType.GENERAL, null, TicketStatus.CANCELLED,
                LocalDateTime.now(), user, event);

        when(ticketRepository.findByTicketCode("TKT-004")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.markAsUsed("TKT-004"))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }
}