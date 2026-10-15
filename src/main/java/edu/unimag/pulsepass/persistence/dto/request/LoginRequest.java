package edu.unimag.pulsepass.persistence.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        // Username o email (FR-SEC-AUTH-002).
        @NotBlank(message = "Identifier is required")
        String identifier, 

        @NotBlank(message = "Password is required")
        String password) {

    @Override 
    public String toString() {
        return "LoginRequest[identifier=" + identifier + ", password=****]"; 
    }

}
