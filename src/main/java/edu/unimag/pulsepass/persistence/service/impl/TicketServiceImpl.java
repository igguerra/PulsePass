package edu.unimag.pulsepass.persistence.service.impl;

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
import edu.unimag.pulsepass.persistence.service.TicketPriceCalculator;
import edu.unimag.pulsepass.persistence.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserProfileRepository userProfileRepository;
    private final TicketRepository ticketRepository;
    private final TicketMapper mapper;
    private final TicketPriceCalculator priceCalculator;
    private final AuthorizationService authorizationService;

    public TicketServiceImpl(
            UserRepository userRepository,
            EventRepository eventRepository,
            UserProfileRepository userProfileRepository,
            TicketRepository ticketRepository,
            TicketMapper mapper,
            TicketPriceCalculator priceCalculator,
            AuthorizationService authorizationService) {

        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.userProfileRepository = userProfileRepository;
        this.ticketRepository = ticketRepository;
        this.mapper = mapper;
        this.priceCalculator = priceCalculator;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {

        // BR-TICKET-001
        User user = userRepository.findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + request.userEmail()));

        // FR-SEC-TKT-01: 
        authorizationService.requireSelf(user.getId());

        // BR-TICKET-002
        if (!user.isActive()) {
            throw new BusinessRuleException(
                    "User is not active: " + request.userEmail());
        }

        // BR-TICKET-003
        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found: " + request.eventCode()));

        // BR-TICKET-004
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException(
                    "Event is not published: " + request.eventCode());
        }

        // BR-TICKET-005
        if (!event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Cannot purchase a ticket for a past event: " + request.eventCode());
        }

        // BR-TICKET-006
        if (event.getMinimumAge() > 0) {
            UserProfile profile = userProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User profile not found for: " + request.userEmail()));

            int age = Period.between(profile.getBirthDate(), event.getEventDate().toLocalDate())
                    .getYears();

            if (age < event.getMinimumAge()) {
                throw new BusinessRuleException(
                        "User does not meet minimum age for event: " + request.eventCode());
            }
        }

        // BR-TICKET-007
        long paidTickets = ticketRepository.countPaidTicketsByEventCode(request.eventCode());
        int capacity = event.getVenue().getCapacity();

        if (paidTickets >= capacity) {
            throw new BusinessRuleException(
                    "No capacity available for event: " + request.eventCode());
        }

        BigDecimal price = priceCalculator.calculate(request.type());

        Ticket ticket = new Ticket(
                generateTicketCode(),
                request.type(),
                price,
                TicketStatus.PAID,
                LocalDateTime.now(),
                user,
                event);

        Ticket saved = ticketRepository.save(ticket);

        // BR-TICKET-008
        if (paidTickets + 1 == capacity) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }

        return mapper.toResponse(saved);
    }

    @Override
    public TicketResponse findByCode(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found: " + ticketCode));

        // FR-SEC-TKT-02: propietario, STAFF or ADMIN
        authorizationService.requireOwnerOrStaffOrAdmin(ticket.getUser().getId());

        return mapper.toResponse(ticket);
    }

    @Override
    public List<TicketResponse> findByUserEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        // FR-SEC-TKT-03: unicamente el suser  o un ADMIN 
        authorizationService.requireSelfOrAdmin(user.getId());

        return ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(email)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {
        return ticketRepository.findByEventEventCodeAndStatus(eventCode, TicketStatus.PAID)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found: " + ticketCode));

       
        authorizationService.requireSelfOrAdmin(ticket.getUser().getId());

        // BR-TICKET-010 / BR-TICKET-011
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException(
                    "Only PAID tickets can be cancelled: " + ticketCode);
        }

        // BR-TICKET-012
        if (LocalDateTime.now().isAfter(ticket.getEvent().getEventDate())) {
            throw new BusinessRuleException(
                    "Cannot cancel a ticket after the event date: " + ticketCode);
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket saved = ticketRepository.save(ticket);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found: " + ticketCode));

        // BR-TICKET-013 / BR-TICKET-014
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException(
                    "Only PAID tickets can be marked as used: " + ticketCode);
        }

        ticket.setStatus(TicketStatus.USED);
        Ticket saved = ticketRepository.save(ticket);

        return mapper.toResponse(saved);
    }

    private String generateTicketCode() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}