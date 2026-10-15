package edu.unimag.pulsepass.persistence.controller;

import static edu.unimag.pulsepass.persistence.security.TestPrincipals.as;
import static edu.unimag.pulsepass.persistence.security.TestPrincipals.user;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.unimag.pulsepass.persistence.domain.Role;
import edu.unimag.pulsepass.persistence.dto.response.AuthResponse;
import edu.unimag.pulsepass.persistence.dto.response.AuthenticatedUserResponse;
import edu.unimag.pulsepass.persistence.security.SecurityPrincipal;
import edu.unimag.pulsepass.persistence.security.WebMvcSecurityTest;
import edu.unimag.pulsepass.persistence.service.AuthService;

@WebMvcTest(AuthController.class)
@WebMvcSecurityTest
class AuthControllerTest {

    private static final String PASSWORD = "UnaFraseSeguraLarga2026!";
    private static final String GENERIC_401 = "Authentication is required or credentials are invalid";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService service;

    private String loginBody(String identifier, String password) {
        return """
                {
                    "identifier": "%s",
                    "password": "%s"
                }
                """.formatted(identifier, password);
    }

    // T-SEC-005: login correcto -> 200 con Bearer
    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        when(service.login(any())).thenReturn(new AuthResponse("jwt-firmado", "Bearer", 900));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("andrea", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("jwt-firmado"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    // T-SEC-006 / T-SEC-019: credenciales incorrectas -> 401 genérico, sin eco de la contraseña
    @Test
    void loginWithWrongPasswordReturnsGeneric401() throws Exception {
        when(service.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("andrea", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(GENERIC_401))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(not(containsString(PASSWORD))));
    }

    // FR-SEC-AUTH-004: usuario inactivo -> mismo 401 genérico
    @Test
    void loginOfInactiveUserReturnsGeneric401() throws Exception {
        when(service.login(any())).thenThrow(new DisabledException("User is disabled"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("andrea", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(GENERIC_401));
    }

    // T-SEC-018: request inválido -> 400, sin intentar autenticar
    @Test
    void loginWithBlankFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.identifier").value("Identifier is required"))
                .andExpect(jsonPath("$.details.password").value("Password is required"));

        verify(service, never()).login(any());
    }

    // /me sin token -> 401
    @Test
    void meWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    // FR-SEC-AUTH-010: /me devuelve datos mínimos del usuario autenticado
    @Test
    void meReturnsTheAuthenticatedUser() throws Exception {
        SecurityPrincipal andrea = user();
        when(service.currentUser(andrea))
                .thenReturn(new AuthenticatedUserResponse(1L, "andrea", Set.of(Role.USER)));

        mockMvc.perform(get("/api/auth/me").with(as(andrea)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(service).currentUser(andrea);
    }
}