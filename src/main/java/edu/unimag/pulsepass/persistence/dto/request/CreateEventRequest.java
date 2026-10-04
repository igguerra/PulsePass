package edu.unimag.pulsepass.persistence.dto.request;

import java.time.LocalDateTime;

import edu.unimag.pulsepass.persistence.domain.EventCategory;

// El request no incluye status: todo evento nuevo inicia en DRAFT (BR-EVENT-005).
public record CreateEventRequest(
        String eventCode,
        String name,
        String description,
        EventCategory category,
        LocalDateTime eventDate,
        Integer minimumAge,
        String venueCode
) {}