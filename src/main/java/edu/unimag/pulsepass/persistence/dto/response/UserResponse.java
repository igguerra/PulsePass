package edu.unimag.pulsepass.persistence.dto.response;

import java.time.LocalDate;

// Combina los datos de User y de UserProfile en una sola respuesta.
public record UserResponse(
        Long id,
        String username,
        String email,
        boolean active,
        String firstName,
        String lastName,
        String phone,
        String city,
        LocalDate birthDate
) {}
