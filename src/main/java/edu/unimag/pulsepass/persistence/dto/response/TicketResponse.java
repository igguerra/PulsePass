package edu.unimag.pulsepass.persistence.dto.response;

import edu.unimag.pulsepass.persistence.domain.TicketStatus;
import edu.unimag.pulsepass.persistence.domain.TicketType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(

        Long id,

        String ticketCode,

        TicketType type,

        BigDecimal price,

        TicketStatus status,

        LocalDateTime purchaseDate,

        String userEmail,

        String eventCode,

        String eventName

) {
}