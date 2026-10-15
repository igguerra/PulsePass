package edu.unimag.pulsepass.persistence.dto.response;

public record AuthResponse(

            String accessToken, 
            String tokenType, 
            long expiresIn

) {}
