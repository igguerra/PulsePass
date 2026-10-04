package edu.unimag.pulsepass.persistence.dto.response;

import java.time.LocalDateTime;

import edu.unimag.pulsepass.persistence.domain.EventCategory;
import edu.unimag.pulsepass.persistence.domain.EventStatus;

// Versión resumida para listados: sin descripción ni artistas.
public record EventSummaryResponse(
        Long id,
        String eventCode,
        String name,
        EventCategory category,
        EventStatus status,
        LocalDateTime eventDate,
        String venueName
) {}
