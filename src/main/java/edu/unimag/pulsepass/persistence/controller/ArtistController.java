package edu.unimag.pulsepass.persistence.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unimag.pulsepass.persistence.service.ArtistService;
import edu.unimag.pulsepass.persistence.dto.response.ArtistResponse;

@RestController 
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService service; 

    public ArtistController(ArtistService service) {
        this.service = service; 
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id)); 
    }

    @GetMapping("/by-stage-name")
    public ResponseEntity<ArtistResponse> findByStageName(@RequestParam String stageName) {
        return ResponseEntity.ok(service.findByStageName(stageName)); 
    }

    @GetMapping("/active")
    public ResponseEntity<List<ArtistResponse>> findActiveArtist() {
        return ResponseEntity.ok(service.findActiveArtists()); 
    }


}
