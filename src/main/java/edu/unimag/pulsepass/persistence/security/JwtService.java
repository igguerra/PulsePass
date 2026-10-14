package edu.unimag.pulsepass.persistence.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service 
public class JwtService {

    private static final String EXPECTED_ALGORITHM = "HS256"; 
    private static final long CLOCK_SKEW_SECONDS = 30; 

    private final JwtProperties properties; 
    private final Clock clock; 
    private final SecretKey signingKey; 
    private final JwtParser parser; 

    @Autowired 
    public JwtService(JwtProperties properties) {
        this(properties, Clock.systemUTC()); 
    }

    JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties; 
        this.clock = clock; 
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secretBase64())); 
        this.parser = Jwts.parser()
                            .verifyWith(signingKey)
                            .requireIssuer(properties.issuer())
                            .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                            .clock(() -> Date.from(clock.instant()))
                            .build(); 
    }

    public IssuedToken issue(Long userId) {
        Instant now = clock.instant(); 
        Instant expiresAt = now.plus(properties.accessTokenTtl()); 

        String token = Jwts.builder()
                            .subject(String.valueOf(userId))
                            .issuer(properties.issuer())
                            .issuedAt(Date.from(now))
                            .expiration(Date.from(expiresAt))
                            .id(UUID.randomUUID().toString())
                            .signWith(signingKey, Jwts.SIG.HS256)
                            .compact(); 

        return new IssuedToken(token, expiresAt); 
    }

    public Long extractUserId(String token) {
        try {
            Jws<Claims> jws = parser.parseSignedClaims(token); 

            if (!EXPECTED_ALGORITHM.equals(jws.getHeader().getAlgorithm())) {
                throw invalidToken(); 
            }

            Claims claims = jws.getPayload(); 
            validateRequiredClaims(claims); 

            return Long.valueOf(claims.getSubject()); 

        } catch (JwtException | IllegalArgumentException ex) {
            throw invalidToken(); 
        }
    }

    private void validateRequiredClaims(Claims claims) {
        Instant latestAcceptable = clock.instant().plusSeconds(CLOCK_SKEW_SECONDS); 

        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw invalidToken(); 
        }

        if (claims.getExpiration() == null) {
            throw invalidToken(); 
        }

        if (claims.getIssuedAt() == null || claims.getIssuedAt().toInstant().isAfter(latestAcceptable)) {
            throw invalidToken(); 
        }

        if (claims.getId() == null || claims.getId().isBlank()) {
            throw invalidToken(); 
        }
    }

    private BadCredentialsException invalidToken() {
        return new BadCredentialsException("Invalid or expired token"); 
    }

    public record IssuedToken(String token, Instant expiresAt) {} 

}
