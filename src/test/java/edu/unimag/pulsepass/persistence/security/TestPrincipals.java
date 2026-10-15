package edu.unimag.pulsepass.persistence.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import edu.unimag.pulsepass.persistence.domain.Role;

public final class TestPrincipals {

    private TestPrincipals() {
    }

    public static SecurityPrincipal user() {
        return principal(1L, "andrea", true, Role.USER);
    }

    public static SecurityPrincipal staff() {
        return principal(2L, "staff", true, Role.STAFF);
    }

    public static SecurityPrincipal admin() {
        return principal(3L, "admin", true, Role.ADMIN);
    }

    public static SecurityPrincipal principal(Long id, String username, boolean active, Role... roles) {
        return new SecurityPrincipal(id, username, "irrelevant-hash", active, Set.of(roles));
    }

    // Uso: mockMvc.perform(post(...).with(as(admin())))
    public static RequestPostProcessor as(SecurityPrincipal principal) {
        return authentication(UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()));
    }
}
