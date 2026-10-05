package edu.unimag.pulsepass.persistence.dto.request;

import java.time.LocalDate;

// El request no incluye active: todo usuario nuevo inicia activo (BR-USER-003).
public record RegisterUserRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String city,
        LocalDate birthDate
) {}