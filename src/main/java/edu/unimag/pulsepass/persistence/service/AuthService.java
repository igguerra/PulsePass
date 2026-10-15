package edu.unimag.pulsepass.persistence.service;

import edu.unimag.pulsepass.persistence.dto.request.LoginRequest;
import edu.unimag.pulsepass.persistence.dto.response.AuthResponse;
import edu.unimag.pulsepass.persistence.dto.response.AuthenticatedUserResponse;
import edu.unimag.pulsepass.persistence.security.SecurityPrincipal;

public interface AuthService {

    AuthResponse login(LoginRequest request); 

    AuthenticatedUserResponse currentUser(SecurityPrincipal principal); 

}
