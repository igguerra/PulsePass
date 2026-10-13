package edu.unimag.pulsepass.persistence.security;

import java.time.Duration;
import java.util.Base64;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsepass.security.jwt")
public record JwtProperties(

    String issuer, 
    String secretBase64, 
    Duration accessTokenTtl

) {

    private static final int MIN_KEY_BYTES = 32; 

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("pulsepass.security.jwt.issuer is required"); 
        }
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException("PULSEPASS_JWT_SECRET_BASE64 is required"); 
        }

        byte[] decodedKey; 
        try {
            decodedKey = Base64.getDecoder().decode(secretBase64) ;
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("PULSEPASS_JWT_SECRET_BASE64 is not valid Base64", ex); 
        }

        if (decodedKey.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("PULSEPASS_JWT_SECRET_BASE64 must decode to at least" + MIN_KEY_BYTES + "bytes"); 
        }
        if (accessTokenTtl == null || accessTokenTtl.isZero() || accessTokenTtl.isNegative()) {
            throw new IllegalStateException("pulsepass.security.jwt.access-token-ttl must be positive");  
        }
    }
}
