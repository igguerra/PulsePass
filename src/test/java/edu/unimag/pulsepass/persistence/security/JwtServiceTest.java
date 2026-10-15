package edu.unimag.pulsepass.persistence.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String KEY = "G1ceX3+BbDwmpg4pR/QKLY0CAgTPSyBWk0Ku4meVmtM=";
    private static final String OTHER_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private static final String ISSUER = "pulsepass-api-test";
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    private final JwtService jwtService = serviceAt(NOW);

    private static JwtService serviceAt(Instant instant) {
        return service(KEY, ISSUER, instant);
    }

    private static JwtService service(String key, String issuer, Instant instant) {
        return new JwtService(
                new JwtProperties(issuer, key, TTL),
                Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Test
    void issuedTokenIsValidAndReturnsTheUserId() {
        JwtService.IssuedToken issued = jwtService.issue(42L);

        assertThat(jwtService.extractUserId(issued.token())).isEqualTo(42L);
        assertThat(issued.expiresAt()).isEqualTo(NOW.plus(TTL));
    }

    // PRD 12.1: sub = ID, iss, iat, exp, jti; HS256; sin roles en el token
    @Test
    void issuedTokenContainsTheRequiredClaims() {
        String token = jwtService.issue(42L).token();

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(KEY));
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(NOW))
                .build()
                .parseSignedClaims(token);
        Claims claims = jws.getPayload();

        assertThat(jws.getHeader().getAlgorithm()).isEqualTo("HS256");
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getIssuedAt().toInstant()).isEqualTo(NOW);
        assertThat(claims.getExpiration().toInstant()).isEqualTo(NOW.plus(TTL));
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.get("roles")).isNull();
    }

    @Test
    void eachTokenHasAUniqueId() {
        String first = jwtService.issue(42L).token();
        String second = jwtService.issue(42L).token();

        assertThat(first).isNotEqualTo(second);
    }

    // T-SEC-007: token vencido
    @Test
    void rejectsExpiredToken() {
        String token = jwtService.issue(42L).token();
        JwtService sixteenMinutesLater = serviceAt(NOW.plus(Duration.ofMinutes(16)));

        assertThatThrownBy(() -> sixteenMinutesLater.extractUserId(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    // T-SEC-008: payload alterado -> la firma ya no coincide
    @Test
    void rejectsTokenWithTamperedPayload() {
        String[] userToken = jwtService.issue(42L).token().split("\\.");
        String[] adminToken = jwtService.issue(1L).token().split("\\.");

        // Header y firma del token 42, pero el contenido del token 1.
        String tampered = userToken[0] + "." + adminToken[1] + "." + userToken[2];

        assertThatThrownBy(() -> jwtService.extractUserId(tampered))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String foreignToken = service(OTHER_KEY, ISSUER, NOW).issue(42L).token();

        assertThatThrownBy(() -> jwtService.extractUserId(foreignToken))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsTokenFromAnotherIssuer() {
        String foreignToken = service(KEY, "otra-api", NOW).issue(42L).token();

        assertThatThrownBy(() -> jwtService.extractUserId(foreignToken))
                .isInstanceOf(BadCredentialsException.class);
    }

    // PRD 12.2: no se acepta alg=none ni tokens sin firma
    @Test
    void rejectsUnsignedToken() {
        String unsigned = Jwts.builder()
                .subject("42")
                .issuer(ISSUER)
                .issuedAt(Date.from(NOW))
                .expiration(Date.from(NOW.plus(TTL)))
                .id("abc")
                .compact();

        assertThatThrownBy(() -> jwtService.extractUserId(unsigned))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsMalformedToken() {
        assertThatThrownBy(() -> jwtService.extractUserId("esto-no-es-un-jwt"))
                .isInstanceOf(BadCredentialsException.class);
    }
}