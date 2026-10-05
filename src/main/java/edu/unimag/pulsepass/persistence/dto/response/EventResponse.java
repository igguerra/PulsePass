package edu.unimag.pulsepass.persistence.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import edu.unimag.pulsepass.persistence.domain.EventCategory;
import edu.unimag.pulsepass.persistence.domain.EventStatus;

// Expone venueCode/venueName y una lista de ArtistResponse en lugar de las entidades Venue y Artist.
public record EventResponse(
        Long id,
        String eventCode,
        String name,
        String description,
        EventCategory category,
        EventStatus status,
        LocalDateTime eventDate,
        Integer minimumAge,
        String venueCode,
        String venueName,
        List<ArtistResponse> artists
) {}
