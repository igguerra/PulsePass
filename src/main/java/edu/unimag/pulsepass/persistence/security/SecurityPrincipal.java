package edu.unimag.pulsepass.persistence.security;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import edu.unimag.pulsepass.persistence.domain.Role;
import edu.unimag.pulsepass.persistence.domain.UserCredential;

public record SecurityPrincipal(

    Long userId, 
    String username, 
    String password, 
    boolean active, 
    Set<Role> roles 

) implements UserDetails {

    public SecurityPrincipal {
        roles = Set.copyOf(roles); 
    }

    public static SecurityPrincipal from(UserCredential credential) {
        return new SecurityPrincipal(
            credential.getUserId(), 
            credential.getUser().getUsername(),
            credential.getPasswordHash(), 
            credential.getUser().isActive(), 
            credential.getRoles()
        ); 
    }

    public boolean hasRole(Role role) {
        return roles.contains(role); 
    }

    @Override 
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role.authority()))
                        .toList(); 
    }

    @Override 
    public String getPassword() {
        return password; 
    }

    @Override 
    public String getUsername() {
        return username; 
    }

    @Override 
    public boolean isEnabled() {
        return active; 
    }

    @Override 
    public String toString() {
        return "SecurityPrincipal[userId=\" + userId + \", username=\" + username + \", roles=\" + roles + \"]"; 
    }

}
