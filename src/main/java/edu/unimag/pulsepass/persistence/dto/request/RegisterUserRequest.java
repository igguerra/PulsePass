package edu.unimag.pulsepass.persistence.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// El request no incluye active: todo usuario nuevo inicia activo (BR-USER-003).
public record RegisterUserRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String phone,
        String city,

        @NotNull(message = "Birth date is required")
        LocalDate birthDate
) {}