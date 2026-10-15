package edu.unimag.pulsepass.persistence.security;

import static edu.unimag.pulsepass.persistence.security.TestPrincipals.admin;
import static edu.unimag.pulsepass.persistence.security.TestPrincipals.principal;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.unimag.pulsepass.persistence.controller.EventController;
import edu.unimag.pulsepass.persistence.domain.EventCategory;
import edu.unimag.pulsepass.persistence.domain.EventStatus;
import edu.unimag.pulsepass.persistence.domain.Role;
import edu.unimag.pulsepass.persistence.dto.response.EventResponse;
import edu.unimag.pulsepass.persistence.service.EventService;

@WebMvcTest(EventController.class)
@WebMvcSecurityTest
class JwtAuthenticationFilterTest {

    private static final String GENERIC_401 = "Authentication is required or credentials are invalid";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private EventService eventService;

    private EventResponse publishedEvent() {
        return new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival de musica",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 15, 20, 0),
                18, "VEN-SMR-01", "Marina Convention Center", List.of());
    }

    // Token válido + usuario activo: el filtro deja la identidad y los roles en el contexto
    @Test
    void validTokenAuthenticatesTheUser() throws Exception {
        when(jwtService.extractUserId("valid-token")).thenReturn(3L);
        when(userDetailsService.loadUserById(3L)).thenReturn(admin());
        when(eventService.publish("CMF-2026")).thenReturn(publishedEvent());

        mockMvc.perform(patch("/api/events/CMF-2026/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isOk());

        verify(eventService).publish("CMF-2026");
    }

    // T-SEC-007 / T-SEC-008 / T-SEC-019: token inválido -> 401 genérico, sin invocar Service
    @Test
    void invalidTokenReturns401WithoutReachingTheService() throws Exception {
        when(jwtService.extractUserId("bad-token")).thenThrow(new BadCredentialsException("Invalid or expired token"));

        mockMvc.perform(patch("/api/events/CMF-2026/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(GENERIC_401))
                .andExpect(jsonPath("$.trace").doesNotExist());

        verifyNoInteractions(eventService);
    }

    // Si se manda un token, debe ser válido aunque la ruta sea pública
    @Test
    void invalidTokenOnPublicRouteAlsoReturns401() throws Exception {
        when(jwtService.extractUserId("bad-token")).thenThrow(new BadCredentialsException("Invalid or expired token"));

        mockMvc.perform(get("/api/events/published")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(eventService);
    }

    // T-SEC-016: token válido de un usuario desactivado -> 401
    @Test
    void validTokenOfInactiveUserReturns401() throws Exception {
        when(jwtService.extractUserId("valid-token")).thenReturn(3L);
        when(userDetailsService.loadUserById(3L)).thenReturn(principal(3L, "admin", false, Role.ADMIN));

        mockMvc.perform(patch("/api/events/CMF-2026/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(GENERIC_401));

        verifyNoInteractions(eventService);
    }

    // Token válido de un usuario que ya no existe -> 401
    @Test
    void validTokenOfDeletedUserReturns401() throws Exception {
        when(jwtService.extractUserId("valid-token")).thenReturn(99L);
        when(userDetailsService.loadUserById(99L)).thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(patch("/api/events/CMF-2026/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(eventService);
    }

    // Un header que no es Bearer se ignora: la solicitud sigue como anónima
    @Test
    void nonBearerHeaderIsTreatedAsAnonymous() throws Exception {
        mockMvc.perform(patch("/api/events/CMF-2026/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(jwtService);
        verifyNoInteractions(eventService);
    }
}