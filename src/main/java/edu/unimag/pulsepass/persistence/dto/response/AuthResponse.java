package edu.unimag.pulsepass.persistence.dto.response;

public record AuthResponse(

            String accesToken, 
            String tokenType, 
            Long expiresIn

) {}
