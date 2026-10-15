package edu.unimag.pulsepass.persistence.dto.response;

import java.util.Set;

import edu.unimag.pulsepass.persistence.domain.Role;

public record AuthenticatedUserResponse(

            Long id, 
            String username, 
            Set<Role> roles

) {}
