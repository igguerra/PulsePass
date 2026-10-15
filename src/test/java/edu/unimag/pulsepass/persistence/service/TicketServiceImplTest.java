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
import edu.unimag.pulsepass.persistence.security.AuthorizationService;
import edu.unimag.pulsepass.persistence.security.SecurityPrincipal;
import edu.unimag.pulsepass.persistence.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    private static final Long ANDREA_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long STAFF_ID = 50L;
    private static final Long ADMIN_ID = 99L;
    private static final String ANDREA_EMAIL = "andrea@email.com";
    private static final String EVENT_CODE = "CMF-2026";

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
                ticketRepository, mapper, priceCalculator, new AuthorizationService());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ---------- helpers ----------

    private void authenticateAs(Long userId, Role... roles) {
        SecurityPrincipal principal = new SecurityPrincipal(
                userId, "user" + userId, "hash", true, Set.of(roles));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    
    private User buildUser(boolean active) {
        User user = new User("andrea", ANDREA_EMAIL);
        ReflectionTestUtils.setField(user, "id", ANDREA_ID);
        user.setActive(active);
        return user;
    }

    private Venue buildVenue(int capacity) {
        return new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Calle 1", capacity);
    }

    private Event buildEvent(EventStatus status, LocalDateTime eventDate, int minimumAge, Venue venue) {
        Event event = new Event(EVENT_CODE, "Caribbean Music Fest", EventCategory.MUSIC, status, eventDate, venue);
        event.setMinimumAge(minimumAge);
        return event;
    }

    private Event publishedFutureEvent() {
        return buildEvent(EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 0, buildVenue(3));
    }

    // Un ticket de Andrea.
    private Ticket buildTicket(String ticketCode, TicketStatus status) {
        return new Ticket(ticketCode, TicketType.GENERAL, null, status,
                LocalDateTime.now(), buildUser(true), publishedFutureEvent());
    }

    private TicketResponse buildResponse(String ticketCode, TicketStatus status) {
        return new TicketResponse(1L, ticketCode, TicketType.GENERAL, null, status,
                LocalDateTime.now(), ANDREA_EMAIL, EVENT_CODE, "Caribbean Music Fest");
    }

    private PurchaseTicketRequest buildRequest() {
        return new PurchaseTicketRequest(ANDREA_EMAIL, EVENT_CODE, TicketType.GENERAL);
    }

    private void mockHappyPathSaves() {
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(buildResponse("TKT-X", TicketStatus.PAID));
    }

    // ---------- purchase ----------

    // TEST-TICKET-001: valid purchase -> PAID ticket
    @Test
    void shouldPurchaseValidTicket() {
        authenticateAs(ANDREA_ID, Role.USER);
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(buildUser(true)));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(publishedFutureEvent()));
        when(ticketRepository.countPaidTicketsByEventCode(EVENT_CODE)).thenReturn(0L);
        mockHappyPathSaves();

        TicketResponse result = service.purchase(buildRequest());

        assertThat(result.status()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository).save(any(Ticket.class));
    }

    // TEST-TICKET-002: -> ResourceNotFoundException
    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(ticketRepository, never()).save(any());
    }

    // T-SEC-010: 
    @Test
    void shouldDenyPurchaseWithAnotherUsersEmail() {
        authenticateAs(OTHER_USER_ID, Role.USER);
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(buildUser(true)));

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(AccessDeniedException.class);

        verify(eventRepository, never()).findByEventCode(any());
        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-003: inactive user -> BusinessRuleException
    @Test
    void shouldNotPurchaseWhenUserIsInactive() {
        authenticateAs(ANDREA_ID, Role.USER);
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(buildUser(false)));

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-004: DRAFT event -> BusinessRuleException
    @Test
    void shouldNotPurchaseWhenEventIsDraft() {
        authenticateAs(ANDREA_ID, Role.USER);
        Event event = buildEvent(EventStatus.DRAFT, LocalDateTime.now().plusDays(10), 0, buildVenue(3));
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(buildUser(true)));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-005: CANCELLED event -> BusinessRuleException
    @Test
    void shouldNotPurchaseWhenEventIsCancelled() {
        authenticateAs(ANDREA_ID, Role.USER);
        Event event = buildEvent(EventStatus.CANCELLED, LocalDateTime.now().plusDays(10), 0, buildVenue(3));
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(buildUser(true)));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-006: -> BusinessRuleException
    @Test
    void shouldNotPurchaseWhenUserIsUnderage() {
        authenticateAs(ANDREA_ID, Role.USER);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(10);
        Event event = buildEvent(EventStatus.PUBLISHED, eventDate, 18, buildVenue(3));
        User user = buildUser(true);

        UserProfile profile = new UserProfile("Laura", "Gomez", user);
        profile.setBirthDate(eventDate.toLocalDate().minusYears(17));

        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(userProfileRepository.findByUserId(ANDREA_ID)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-007: no capacity -> BusinessRuleException
    @Test
    void shouldNotPurchaseWhenThereIsNoCapacity() {
        authenticateAs(ANDREA_ID, Role.USER);
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(buildUser(true)));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(publishedFutureEvent()));
        when(ticketRepository.countPaidTicketsByEventCode(EVENT_CODE)).thenReturn(3L);

        assertThatThrownBy(() -> service.purchase(buildRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // TEST-TICKET-008: last available ticket -> saved and event becomes SOLD_OUT
    @Test
    void shouldMarkEventAsSoldOutWhenCapacityIsReached() {
        authenticateAs(ANDREA_ID, Role.USER);
        Event event = publishedFutureEvent();
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(buildUser(true)));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(ticketRepository.countPaidTicketsByEventCode(EVENT_CODE)).thenReturn(2L);
        mockHappyPathSaves();

        service.purchase(buildRequest());

        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        verify(eventRepository).save(event);
    }

    // ---------- findByCode ----------

    @Test
    void shouldFindOwnTicketByCode() {
        authenticateAs(ANDREA_ID, Role.USER);
        Ticket ticket = buildTicket("TKT-001", TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TKT-001")).thenReturn(Optional.of(ticket));
        when(mapper.toResponse(ticket)).thenReturn(buildResponse("TKT-001", TicketStatus.PAID));

        assertThat(service.findByCode("TKT-001").ticketCode()).isEqualTo("TKT-001");
    }

    @Test
    void shouldLetStaffFindAnyTicketByCode() {
        authenticateAs(STAFF_ID, Role.STAFF);
        Ticket ticket = buildTicket("TKT-001", TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TKT-001")).thenReturn(Optional.of(ticket));
        when(mapper.toResponse(ticket)).thenReturn(buildResponse("TKT-001", TicketStatus.PAID));

        assertThat(service.findByCode("TKT-001").ticketCode()).isEqualTo("TKT-001");
    }

    // T-SEC-009
    @Test
    void shouldDenyFindingAnotherUsersTicket() {
        authenticateAs(OTHER_USER_ID, Role.USER);
        when(ticketRepository.findByTicketCode("TKT-001"))
                .thenReturn(Optional.of(buildTicket("TKT-001", TicketStatus.PAID)));

        assertThatThrownBy(() -> service.findByCode("TKT-001"))
                .isInstanceOf(AccessDeniedException.class);

        verify(mapper, never()).toResponse(any());
    }

    // ---------- findByUserEmail ----------

    @Test
    void shouldFindOwnTicketsByEmail() {
        authenticateAs(ANDREA_ID, Role.USER);
        Ticket ticket = buildTicket("TKT-001", TicketStatus.PAID);
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(buildUser(true)));
        when(ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(ANDREA_EMAIL))
                .thenReturn(List.of(ticket));
        when(mapper.toResponse(ticket)).thenReturn(buildResponse("TKT-001", TicketStatus.PAID));

        assertThat(service.findByUserEmail(ANDREA_EMAIL)).hasSize(1);
    }

    @Test
    void shouldDenyFindingAnotherUsersTickets() {
        authenticateAs(OTHER_USER_ID, Role.USER);
        when(userRepository.findByEmailIgnoreCase(ANDREA_EMAIL)).thenReturn(Optional.of(buildUser(true)));

        assertThatThrownBy(() -> service.findByUserEmail(ANDREA_EMAIL))
                .isInstanceOf(AccessDeniedException.class);

        verify(ticketRepository, never()).findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(any());
    }

    // ---------- cancel ----------

    // TEST-TICKET-009 / T-SEC-011: owner cancels a PAID ticket -> CANCELLED
    @Test
    void shouldCancelOwnPaidTicket() {
        authenticateAs(ANDREA_ID, Role.USER);
        Ticket ticket = buildTicket("TKT-001", TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TKT-001")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(buildResponse("TKT-001", TicketStatus.CANCELLED));

        TicketResponse result = service.cancel("TKT-001");

        assertThat(result.status()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
    }

    // TEST-TICKET-010 -> BusinessRuleException
    @Test
    void shouldNotCancelUsedTicket() {
        authenticateAs(ANDREA_ID, Role.USER);
        when(ticketRepository.findByTicketCode("TKT-002"))
                .thenReturn(Optional.of(buildTicket("TKT-002", TicketStatus.USED)));

        assertThatThrownBy(() -> service.cancel("TKT-002"))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldDenyCancellingAnotherUsersTicket() {
        authenticateAs(OTHER_USER_ID, Role.USER);
        Ticket ticket = buildTicket("TKT-001", TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TKT-001")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.cancel("TKT-001"))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldLetAdminCancelAnotherUsersTicket() {
        authenticateAs(ADMIN_ID, Role.ADMIN);
        Ticket ticket = buildTicket("TKT-001", TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TKT-001")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(buildResponse("TKT-001", TicketStatus.CANCELLED));

        assertThat(service.cancel("TKT-001").status()).isEqualTo(TicketStatus.CANCELLED);
    }

    // ---------- markAsUsed (route-protected: STAFF/ADMIN) ----------

    // TEST-TICKET-011: marking a PAID ticket as used -> USED
    @Test
    void shouldMarkPaidTicketAsUsed() {
        authenticateAs(STAFF_ID, Role.STAFF);
        Ticket ticket = buildTicket("TKT-003", TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TKT-003")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(buildResponse("TKT-003", TicketStatus.USED));

        TicketResponse result = service.markAsUsed("TKT-003");

        assertThat(result.status()).isEqualTo(TicketStatus.USED);
    }

    // TEST-TICKET-012: using a CANCELLED ticket -> BusinessRuleException
    @Test
    void shouldNotUseCancelledTicket() {
        authenticateAs(STAFF_ID, Role.STAFF);
        when(ticketRepository.findByTicketCode("TKT-004"))
                .thenReturn(Optional.of(buildTicket("TKT-004", TicketStatus.CANCELLED)));

        assertThatThrownBy(() -> service.markAsUsed("TKT-004"))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }
}