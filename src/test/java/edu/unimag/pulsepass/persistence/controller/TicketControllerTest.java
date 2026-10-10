package edu.unimag.pulsepass.persistence.controller;

import edu.unimag.pulsepass.persistence.domain.TicketStatus;
import edu.unimag.pulsepass.persistence.domain.TicketType;
import edu.unimag.pulsepass.persistence.dto.response.TicketResponse;
import edu.unimag.pulsepass.persistence.exception.BusinessRuleException;
import edu.unimag.pulsepass.persistence.exception.GlobalExceptionHandler;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.service.TicketService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService service;

    private TicketResponse buildTicketResponse(String ticketCode, TicketStatus ticketStatus) {
        return new TicketResponse(
                1L, ticketCode, TicketType.VIP, new BigDecimal("200.00"), ticketStatus,
                LocalDateTime.now(), "andrea@email.com", "CMF-2026", "Caribbean Music Fest 2026");
    }

    private String purchaseBody() {
        return """
                {
                    "userEmail": "andrea@email.com",
                    "eventCode": "CMF-2026",
                    "type": "VIP"
                }
                """;
    }

    // TEST-CTRL-TKT-001: valid purchase -> 201
    @Test
    void shouldPurchaseTicket() throws Exception {
        when(service.purchase(any())).thenReturn(buildTicketResponse("TKT-AAAA1111", TicketStatus.PAID));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseBody()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-AAAA1111"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.type").value("VIP"))
                .andExpect(jsonPath("$.userEmail").value("andrea@email.com"))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(service).purchase(any());
    }

    // TEST-CTRL-TKT-002: invalid request -> 400 and the service is never called
    @Test
    void shouldReturn400WhenPurchaseRequestIsInvalid() throws Exception {
        String invalidBody = """
                {
                    "userEmail": "not-an-email",
                    "eventCode": "",
                    "type": null
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details.userEmail").value("User email format is invalid"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.type").value("Ticket type is required"));

        verify(service, never()).purchase(any());
    }

    // TEST-CTRL-TKT-003: user does not exist -> 404
    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        when(service.purchase(any())).thenThrow(
                new ResourceNotFoundException("User not found: andrea@email.com"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseBody()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: andrea@email.com"));

        verify(service).purchase(any());
    }

    // TEST-CTRL-TKT-004: business rule violated -> 409
    @Test
    void shouldReturn409WhenPurchaseViolatesBusinessRule() throws Exception {
        when(service.purchase(any())).thenThrow(
                new BusinessRuleException("No capacity available for event: CMF-2026"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseBody()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("No capacity available for event: CMF-2026"));

        verify(service).purchase(any());
    }

    // TEST-CTRL-TKT-005: find ticket by code -> 200
    @Test
    void shouldFindTicketByCode() throws Exception {
        when(service.findByCode("TKT-AAAA1111"))
                .thenReturn(buildTicketResponse("TKT-AAAA1111", TicketStatus.PAID));

        mockMvc.perform(get("/api/tickets/{ticketCode}", "TKT-AAAA1111"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-AAAA1111"))
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(service).findByCode("TKT-AAAA1111");
    }

    // TEST-CTRL-TKT-006: tickets by user -> 200
    @Test
    void shouldFindTicketsByUserEmail() throws Exception {
        when(service.findByUserEmail("andrea@email.com")).thenReturn(List.of(
                buildTicketResponse("TKT-AAAA1111", TicketStatus.PAID),
                buildTicketResponse("TKT-BBBB2222", TicketStatus.USED)));

        mockMvc.perform(get("/api/tickets/by-user").param("email", "andrea@email.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ticketCode").value("TKT-AAAA1111"))
                .andExpect(jsonPath("$[1].ticketCode").value("TKT-BBBB2222"));

        verify(service).findByUserEmail("andrea@email.com");
    }

    // TEST-CTRL-TKT-007: PAID tickets by event -> 200
    @Test
    void shouldFindPaidTicketsByEvent() throws Exception {
        when(service.findPaidTicketsByEvent("CMF-2026")).thenReturn(List.of(
                buildTicketResponse("TKT-AAAA1111", TicketStatus.PAID)));

        mockMvc.perform(get("/api/events/{eventCode}/tickets/paid", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(service).findPaidTicketsByEvent("CMF-2026");
    }

    // TEST-CTRL-TKT-008: valid cancellation -> 200
    @Test
    void shouldCancelTicket() throws Exception {
        when(service.cancel("TKT-AAAA1111"))
                .thenReturn(buildTicketResponse("TKT-AAAA1111", TicketStatus.CANCELLED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TKT-AAAA1111"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(service).cancel("TKT-AAAA1111");
    }

    // TEST-CTRL-TKT-009: invalid cancellation -> 409
    @Test
    void shouldReturn409WhenCancellationIsNotAllowed() throws Exception {
        when(service.cancel("TKT-BBBB2222")).thenThrow(
                new BusinessRuleException("Only PAID tickets can be cancelled: TKT-BBBB2222"));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TKT-BBBB2222"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Only PAID tickets can be cancelled: TKT-BBBB2222"));

        verify(service).cancel("TKT-BBBB2222");
    }

    // TEST-CTRL-TKT-010: valid use -> 200
    @Test
    void shouldMarkTicketAsUsed() throws Exception {
        when(service.markAsUsed("TKT-AAAA1111"))
                .thenReturn(buildTicketResponse("TKT-AAAA1111", TicketStatus.USED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TKT-AAAA1111"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("USED"));

        verify(service).markAsUsed("TKT-AAAA1111");
    }

    // TEST-CTRL-TKT-011: invalid use -> 409
    @Test
    void shouldReturn409WhenTicketCannotBeUsed() throws Exception {
        when(service.markAsUsed("TKT-CCCC3333")).thenThrow(
                new BusinessRuleException("Only PAID tickets can be marked as used: TKT-CCCC3333"));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TKT-CCCC3333"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Only PAID tickets can be marked as used: TKT-CCCC3333"));

        verify(service).markAsUsed("TKT-CCCC3333");
    }
}