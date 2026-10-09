package edu.unimag.pulsepass.persistence.dto.request;

import java.time.LocalDateTime;

import edu.unimag.pulsepass.persistence.domain.EventCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// El request no incluye status: todo evento nuevo inicia en DRAFT (BR-EVENT-005).
public record CreateEventRequest(

        @NotBlank(message = "Event code is required")
        String eventCode,

        @NotBlank(message = "Name is required")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Category is required")
        EventCategory category,

        @NotNull(message = "Event date is required")
        LocalDateTime eventDate,

        @NotNull(message = "Minimum age is required")
        @Min(value = 0, message = "Minimum age must be 0 or greater")
        Integer minimumAge,

        @NotBlank(message = "Venue code is required")
        String venueCode
) {}