package edu.unimag.pulsepass.persistence.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 
public class RestAccessDeniedHandler implements AccessDeniedHandler{

    private final HandlerExceptionResolver resolver; 

    public RestAccessDeniedHandler(
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
            this.resolver = resolver; 
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accesDeniedException) {
        resolver.resolveException(request, response, null, accesDeniedException); 
    }

}
