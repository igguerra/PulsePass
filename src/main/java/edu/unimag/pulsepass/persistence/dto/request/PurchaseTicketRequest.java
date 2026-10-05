package edu.unimag.pulsepass.persistence.dto.request;

import edu.unimag.pulsepass.persistence.domain.TicketType;

public record PurchaseTicketRequest(

        String userEmail,

        String eventCode,

        TicketType type

) {
}