package edu.unimag.pulsepass.persistence.security;

import edu.unimag.pulsepass.persistence.domain.Role;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationServiceTest {

    private final AuthorizationService authorizationService = new AuthorizationService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId, Role... roles) {
        SecurityPrincipal principal = new SecurityPrincipal(
                userId, "user" + userId, "hash", true, Set.of(roles));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    void shouldReturnCurrentPrincipal() {
        authenticateAs(1L, Role.USER);

        assertThat(authorizationService.currentPrincipal().userId()).isEqualTo(1L);
    }

    @Test
    void shouldDenyWhenThereIsNoAuthentication() {
        assertThatThrownBy(() -> authorizationService.currentPrincipal())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldAllowTheOwnerOnRequireSelf() {
        authenticateAs(1L, Role.USER);

        assertThatCode(() -> authorizationService.requireSelf(1L)).doesNotThrowAnyException();
    }

    @Test
    void shouldDenyAnotherUserOnRequireSelf() {
        authenticateAs(2L, Role.USER);

        assertThatThrownBy(() -> authorizationService.requireSelf(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldDenyAdminOnRequireSelfWhenNotTheOwner() {
        authenticateAs(99L, Role.ADMIN);

        assertThatThrownBy(() -> authorizationService.requireSelf(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldAllowAdminOnRequireSelfOrAdmin() {
        authenticateAs(99L, Role.ADMIN);

        assertThatCode(() -> authorizationService.requireSelfOrAdmin(1L)).doesNotThrowAnyException();
    }

    @Test
    void shouldDenyAnotherUserOnRequireSelfOrAdmin() {
        authenticateAs(2L, Role.USER);

        assertThatThrownBy(() -> authorizationService.requireSelfOrAdmin(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldAllowTheOwnerOnRequireOwnerOrStaffOrAdmin() {
        authenticateAs(1L, Role.USER);

        assertThatCode(() -> authorizationService.requireOwnerOrStaffOrAdmin(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowStaffOnRequireOwnerOrStaffOrAdmin() {
        authenticateAs(50L, Role.STAFF);

        assertThatCode(() -> authorizationService.requireOwnerOrStaffOrAdmin(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldDenyAnotherUserOnRequireOwnerOrStaffOrAdmin() {
        authenticateAs(2L, Role.USER);

        assertThatThrownBy(() -> authorizationService.requireOwnerOrStaffOrAdmin(1L))
                .isInstanceOf(AccessDeniedException.class);
    }
}