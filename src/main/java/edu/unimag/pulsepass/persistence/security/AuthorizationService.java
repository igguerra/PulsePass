package edu.unimag.pulsepass.persistence.security;

import edu.unimag.pulsepass.persistence.domain.Role;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationService {

    private static final String ACCESS_DENIED_MESSAGE =
            "You do not have permission to access this resource";

    // Returns the authenticated principal of the current request.
    public SecurityPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
            throw new AccessDeniedException("Authentication is required");
        }

        return principal;
    }

    // Only the owner is allowed (not even ADMIN).
    public void requireSelf(Long ownerUserId) {
        SecurityPrincipal principal = currentPrincipal();

        if (!isOwner(principal, ownerUserId)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    // The owner or an ADMIN is allowed.
    public void requireSelfOrAdmin(Long ownerUserId) {
        SecurityPrincipal principal = currentPrincipal();

        if (!isOwner(principal, ownerUserId) && !principal.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    // The owner, STAFF or ADMIN is allowed.
    public void requireOwnerOrStaffOrAdmin(Long ownerUserId) {
        SecurityPrincipal principal = currentPrincipal();

        if (!isOwner(principal, ownerUserId)
                && !principal.hasRole(Role.STAFF)
                && !principal.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    private boolean isOwner(SecurityPrincipal principal, Long ownerUserId) {
        return ownerUserId != null && ownerUserId.equals(principal.userId());
    }
}