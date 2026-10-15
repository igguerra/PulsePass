package edu.unimag.pulsepass.persistence.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.unimag.pulsepass.persistence.domain.EventCategory;
import edu.unimag.pulsepass.persistence.domain.EventStatus;
import edu.unimag.pulsepass.persistence.dto.response.ArtistResponse;
import edu.unimag.pulsepass.persistence.dto.response.EventResponse;
import edu.unimag.pulsepass.persistence.dto.response.EventSummaryResponse;
import edu.unimag.pulsepass.persistence.exception.BusinessRuleException;
import edu.unimag.pulsepass.persistence.exception.GlobalExceptionHandler;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.service.EventService;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {

    @Autowired 
    private MockMvc mockMvc; 

    @MockitoBean 
    private EventService service; 

    private static final LocalDateTime EVENT_DATE = LocalDateTime.of(2026, 12, 15, 20, 0); 

    private EventResponse buildEventResponse(EventStatus status, List<ArtistResponse> artists){
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

    // TEST-CTRL-EVT-001: valid create -> 201
    @Test 
    void shouldCreateEvent() throws Exception {
        when(service.create(any())).thenReturn(buildEventResponse(EventStatus.DRAFT, List.of()));
        
        mockMvc.perform(post("/api/events")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(createBody("CMF-2026")))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.venueCode").value("VEN-SMR-01")); 
        
        verify(service).create(any()); 
    }

    // TEST-CTRL-EVT-002: invalid request -> 400
    @Test 
    void shoulReturn400WhenCreateRequesIsInvalid() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(createBody("")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required")); 

        verify(service, never()).create(any()); 
    }

    // TEST-CTRL-EVT-003: find existing -> 200
    @Test 
    void shouldFindEventByCode() throws Exception {
        when(service.findByCode(eq("CMF-2026"), anyBoolean())).thenReturn(buildEventResponse(EventStatus.DRAFT, List.of())); 

        mockMvc.perform(get("/api/events/CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.name").value("Caribbean Music Fest 2026"))
                .andExpect(jsonPath("$.minimumAge").value(18)); 

        verify(service).findByCode(eq("CMF-2026"), anyBoolean()); 
    }

    // TEST-CTRL-EVT-004: non-existing -> 404
    @Test 
    void shouldReturn404WhenEventNotFound() throws Exception {
        when(service.findByCode(eq("NOPE-00"), anyBoolean())).thenThrow(new ResourceNotFoundException("Event not found: NOPE-00")); 

        mockMvc.perform(get("/api/events/NOPE-00"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Event not found: NOPE-00")); 

        verify(service).findByCode(eq("NOPE-00"), anyBoolean()); 
    }

    // TEST-CTRL-EVT-005: list published -> 200
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

    // TEST-CTRL-EVT-006: valid publish -> 200
    @Test 
    void shouldPublishEvent() throws Exception {
        when(service.publish("CMF-2026")).thenReturn(buildEventResponse(EventStatus.PUBLISHED, List.of())); 

        mockMvc.perform(patch("/api/events/CMF-2026/publish"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("PUBLISHED")); 

        verify(service).publish("CMF-2026"); 
    }

    // TEST-CTRL-EVT-007: invalid publish -> 409
    @Test 
    void shouldReturn409WhenPublishIsNotAllow() throws Exception {
        when(service.publish("CMF-2026")).thenThrow(new BusinessRuleException("Only DRAFT events can be published")); 

        mockMvc.perform(patch("/api/events/CMF-2026/publish"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Only DRAFT events can be published")); 

        verify(service).publish("CMF-2026"); 
    }

    // TEST-CTRL-EVT-008: add artist -> 200
    @Test
    void shouldAddArtistToEvent() throws Exception {
        ArtistResponse artist = new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true);
        when(service.addArtist("CMF-2026", 1L))
                .thenReturn(buildEventResponse(EventStatus.DRAFT, List.of(artist)));

        mockMvc.perform(post("/api/events/CMF-2026/artists/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.artists.length()").value(1))
                .andExpect(jsonPath("$.artists[0].stageName").value("Solar Beat"));

        verify(service).addArtist("CMF-2026", 1L);
    }

    // TEST-CTRL-EVT-009: find by artist -> 200
    @Test
    void shouldFindEventsByArtist() throws Exception {
        when(service.findByArtist(eq("Solar Beat"), anyBoolean())).thenReturn(List.of(buildEventSummary()));

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(service).findByArtist(eq("Solar Beat"), anyBoolean());
    }

}
