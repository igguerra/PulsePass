package edu.unimag.pulsepass.persistence.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unimag.pulsepass.persistence.domain.Role;
import edu.unimag.pulsepass.persistence.dto.request.CreateEventRequest;
import edu.unimag.pulsepass.persistence.dto.response.EventResponse;
import edu.unimag.pulsepass.persistence.dto.response.EventSummaryResponse;
import edu.unimag.pulsepass.persistence.security.SecurityPrincipal;
import edu.unimag.pulsepass.persistence.service.EventService;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/events")
public class EventController {

    private final EventService service; 

    public EventController(EventService service) {
        this.service = service; 
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(
        @Valid @RequestBody CreateEventRequest request) {

            EventResponse response = service.create(request); 

            return ResponseEntity.status(HttpStatus.CREATED).body(response); 

    }

    @GetMapping("/{eventCode}")
    public ResponseEntity<EventResponse> findByCode(@PathVariable String eventCode, @AuthenticationPrincipal SecurityPrincipal principal) {
        return ResponseEntity.ok(service.findByCode(eventCode, canSeeUnpublished(principal))); 
    }

    @GetMapping("/published")
    public ResponseEntity<List<EventSummaryResponse>> findPublishedEvents() {
        return ResponseEntity.ok(service.findPublishedEvents()); 
    }

    @PatchMapping("/{eventCode}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable String eventCode) {
        return ResponseEntity.ok(service.publish(eventCode)); 
    }

    @PostMapping("/{eventCode}/artists/{artistId}")
    public ResponseEntity<EventResponse> addArtist(
                                            @PathVariable String eventCode, 
                                            @PathVariable Long artistId) {  
        return ResponseEntity.ok(service.addArtist(eventCode, artistId)); 
    }

    @GetMapping("/by-artist")
    public ResponseEntity<List<EventSummaryResponse>> findByArtis(@RequestParam String stageName, @AuthenticationPrincipal SecurityPrincipal principal) {
        return ResponseEntity.ok(service.findByArtist(stageName, canSeeUnpublished(principal))); 
    }

    private boolean canSeeUnpublished(SecurityPrincipal principal) {
        return principal != null 
            && (principal.hasRole(Role.STAFF) || principal.hasRole(Role.ADMIN)); 
    }
}
