package edu.unimag.pulsepass.persistence.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.unimag.pulsepass.persistence.dto.response.VenueResponse;
import edu.unimag.pulsepass.persistence.service.VenueService;

@RestController 
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService service; 

    public VenueController(VenueService service) {
        this.service = service;    
    }

    @GetMapping("/{code}")
    public ResponseEntity<VenueResponse> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code)); 
    }

    @GetMapping("/active")
    public ResponseEntity<List<VenueResponse>> findActiveVenues() {
        return ResponseEntity.ok(service.findActiveVenues()); 
    } 

}
