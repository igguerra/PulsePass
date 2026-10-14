package edu.unimag.pulsepass.persistence.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private static final String BEARER_PREFIX = "Bearer "; 

    private final JwtService jwtService; 
    private final CustomUserDetailsService userDetailsService; 
    private final AuthenticationEntryPoint autheticationEntryPoint; 

    public JwtAuthenticationFilter(JwtService jwtService, 
                                    CustomUserDetailsService userDetailsService, 
                                    AuthenticationEntryPoint authenticationEntryPoint) {  
        this.jwtService = jwtService; 
        this.userDetailsService = userDetailsService; 
        this.autheticationEntryPoint = authenticationEntryPoint;
    }
    
    @Override 
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION); 

        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            filterChain.doFilter(request, response);
            return; 
        }
        
        try {
            String token = header.substring(BEARER_PREFIX.length()).trim(); 
            authenticate(token, request); 
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            autheticationEntryPoint.commence(request, response, ex);
            return ; 
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        Long userId = jwtService.extractUserId(token); 
        SecurityPrincipal principal = userDetailsService.loadUserById(userId); 

        if (!principal.isEnabled()) {
            throw new DisabledException("User is disabled"); 
        }

        UsernamePasswordAuthenticationToken authentication = 
            UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()); 
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext(); 
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

}
