package edu.unimag.pulsepass.persistence.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import edu.unimag.pulsepass.persistence.dto.request.LoginRequest;
import edu.unimag.pulsepass.persistence.dto.response.AuthResponse;
import edu.unimag.pulsepass.persistence.dto.response.AuthenticatedUserResponse;
import edu.unimag.pulsepass.persistence.security.JwtProperties;
import edu.unimag.pulsepass.persistence.security.JwtService;
import edu.unimag.pulsepass.persistence.security.SecurityPrincipal;
import edu.unimag.pulsepass.persistence.service.AuthService;

public class AuthServiceImpl implements AuthService{

    private static final String TOKEN_TYPE = "Bearer"; 

    private final AuthenticationManager authenticationManager; 
    private final JwtService jwtService; 
    private final JwtProperties jwtProperties; 

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService, JwtProperties jwtProperties) {

        this.authenticationManager = authenticationManager; 
        this.jwtService = jwtService; 
        this.jwtProperties = jwtProperties; 

    }

    @Override 
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
             UsernamePasswordAuthenticationToken.unauthenticated(request.identifier(), request.password())); 

        SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal(); 
        JwtService.IssuedToken issued = jwtService.issue(principal.userId()); 

        return new AuthResponse(
                issued.token(), 
                TOKEN_TYPE, 
                jwtProperties.accessTokenTtl().toSeconds()
        ); 
    }

    @Override 
    public AuthenticatedUserResponse currentUser(SecurityPrincipal principal) {
        return new AuthenticatedUserResponse(
            principal.userId(), 
            principal.username(), 
            principal.roles()
        ); 

    }

}
