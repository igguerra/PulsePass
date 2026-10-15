package edu.unimag.pulsepass.persistence.controller;

import static edu.unimag.pulsepass.persistence.security.TestPrincipals.admin;
import static edu.unimag.pulsepass.persistence.security.TestPrincipals.as;
import static edu.unimag.pulsepass.persistence.security.TestPrincipals.staff;
import static edu.unimag.pulsepass.persistence.security.TestPrincipals.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.unimag.pulsepass.persistence.domain.EventCategory;
import edu.unimag.pulsepass.persistence.domain.EventStatus;
import edu.unimag.pulsepass.persistence.dto.response.ArtistResponse;
import edu.unimag.pulsepass.persistence.dto.response.EventResponse;
import edu.unimag.pulsepass.persistence.dto.response.EventSummaryResponse;
import edu.unimag.pulsepass.persistence.exception.BusinessRuleException;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.security.WebMvcSecurityTest;
import edu.unimag.pulsepass.persistence.service.EventService;

@WebMvcTest(EventController.class)
@WebMvcSecurityTest
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService service;

    private static final LocalDateTime EVENT_DATE = LocalDateTime.of(2026, 12, 15, 20, 0);

    private EventResponse buildEventResponse(EventStatus status, List<ArtistResponse> artists) {
        return new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival de musica",
                EventCategory.MUSIC, status, EVENT_DATE, 18, "VEN-SMR-01",
                "Marina Convention Center", artists);
    }

    private EventSummaryResponse buildEventSummary() {
        return new EventSummaryResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026",
                EventCategory.MUSIC, EventStatus.PUBLISHED, EVENT_DATE,
                "Marina Convention Center");
    }

    private String createBody(String eventCode) {
        return """
                {
                    "eventCode": "%s",
                    "name": "Caribbean Music Fest 2026",
                    "description": "Festival de musica",
                    "category": "MUSIC",
                    "eventDate": "2026-12-15T20:00:00",
                    "minimumAge": 18,
                    "venueCode": "VEN-SMR-01"
                }
                """.formatted(eventCode);
    }

    // TEST-CTRL-EVT-001 / T-SEC-004: ADMIN crea evento válido -> 201
    @Test
    void shouldCreateEvent() throws Exception {
        when(service.create(any())).thenReturn(buildEventResponse(EventStatus.DRAFT, List.of()));

        mockMvc.perform(post("/api/events")
                        .with(as(admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("CMF-2026")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.venueCode").value("VEN-SMR-01"));

        verify(service).create(any());
    }

    // TEST-CTRL-EVT-002 / T-SEC-018: ADMIN con request inválido -> 400 de Bean Validation
    @Test
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/events")
                        .with(as(admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"));

        verify(service, never()).create(any());
    }

    // TEST-CTRL-EVT-003: anónimo consulta evento publicado -> 200 (sin privilegios)
    @Test
    void shouldFindEventByCode() throws Exception {
        when(service.findByCode("CMF-2026", false))
                .thenReturn(buildEventResponse(EventStatus.PUBLISHED, List.of()));

        mockMvc.perform(get("/api/events/CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.name").value("Caribbean Music Fest 2026"))
                .andExpect(jsonPath("$.minimumAge").value(18));

        verify(service).findByCode("CMF-2026", false);
    }

    // TEST-CTRL-EVT-004: código inexistente -> 404
    @Test
    void shouldReturn404WhenEventNotFound() throws Exception {
        when(service.findByCode("NOPE-00", false))
                .thenThrow(new ResourceNotFoundException("Event not found: NOPE-00"));

        mockMvc.perform(get("/api/events/NOPE-00"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Event not found: NOPE-00"));

        verify(service).findByCode("NOPE-00", false);
    }

    // TEST-CTRL-EVT-005 / T-SEC-001: anónimo lista publicados -> 200
    @Test
    void shouldFindPublishedEvents() throws Exception {
        when(service.findPublishedEvents()).thenReturn(List.of(buildEventSummary()));

        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));

        verify(service).findPublishedEvents();
    }

    // TEST-CTRL-EVT-006 / T-SEC-014: ADMIN publica -> 200
    @Test
    void shouldPublishEvent() throws Exception {
        when(service.publish("CMF-2026")).thenReturn(buildEventResponse(EventStatus.PUBLISHED, List.of()));

        mockMvc.perform(patch("/api/events/CMF-2026/publish").with(as(admin())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(service).publish("CMF-2026");
    }

    // TEST-CTRL-EVT-007: ADMIN, publicación no permitida -> 409
    @Test
    void shouldReturn409WhenPublishIsNotAllowed() throws Exception {
        when(service.publish("CMF-2026")).thenThrow(new BusinessRuleException("Only DRAFT events can be published"));

        mockMvc.perform(patch("/api/events/CMF-2026/publish").with(as(admin())))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Only DRAFT events can be published"));

        verify(service).publish("CMF-2026");
    }

    // TEST-CTRL-EVT-008: ADMIN agrega artista -> 200
    @Test
    void shouldAddArtistToEvent() throws Exception {
        ArtistResponse artist = new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true);
        when(service.addArtist("CMF-2026", 1L))
                .thenReturn(buildEventResponse(EventStatus.DRAFT, List.of(artist)));

        mockMvc.perform(post("/api/events/CMF-2026/artists/1").with(as(admin())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.artists.length()").value(1))
                .andExpect(jsonPath("$.artists[0].stageName").value("Solar Beat"));

        verify(service).addArtist("CMF-2026", 1L);
    }

    // TEST-CTRL-EVT-009: anónimo busca por artista -> 200 (sin privilegios)
    @Test
    void shouldFindEventsByArtist() throws Exception {
        when(service.findByArtist("Solar Beat", false)).thenReturn(List.of(buildEventSummary()));

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(service).findByArtist("Solar Beat", false);
    }


    // ---------- Seguridad ----------

    // T-SEC-002: anónimo intenta crear -> 401 con ErrorResponse, sin invocar Service
    @Test
    void anonymousCannotCreateEvent() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("CMF-2026")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verifyNoInteractions(service);
    }

    // T-SEC-003: USER intenta crear -> 403, sin invocar Service
    @Test
    void userCannotCreateEvent() throws Exception {
        mockMvc.perform(post("/api/events")
                        .with(as(user()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("CMF-2026")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verifyNoInteractions(service);
    }

    // STAFF no puede publicar: publicar es exclusivo de ADMIN
    @Test
    void staffCannotPublishEvent() throws Exception {
        mockMvc.perform(patch("/api/events/CMF-2026/publish").with(as(staff())))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    // T-SEC-017: un USER tampoco tiene privilegios para ver borradores
    @Test
    void userQueriesEventWithoutPrivileges() throws Exception {
        when(service.findByCode("CMF-2026", false))
                .thenThrow(new ResourceNotFoundException("Event not found: CMF-2026"));

        mockMvc.perform(get("/api/events/CMF-2026").with(as(user())))
                .andExpect(status().isNotFound());

        verify(service).findByCode("CMF-2026", false);
    }

    // T-SEC-017: STAFF sí puede ver eventos no publicados
    @Test
    void staffQueriesEventWithPrivileges() throws Exception {
        when(service.findByCode("CMF-2026", true))
                .thenReturn(buildEventResponse(EventStatus.DRAFT, List.of()));

        mockMvc.perform(get("/api/events/CMF-2026").with(as(staff())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(service).findByCode("CMF-2026", true);
    }

    // T-SEC-017: ADMIN busca por artista con privilegios
    @Test
    void adminSearchesByArtistWithPrivileges() throws Exception {
        when(service.findByArtist("Solar Beat", true)).thenReturn(List.of(buildEventSummary()));

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat").with(as(admin())))
                .andExpect(status().isOk());

        verify(service).findByArtist("Solar Beat", true);
    }
}