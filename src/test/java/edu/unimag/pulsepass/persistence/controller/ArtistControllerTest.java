package edu.unimag.pulsepass.persistence.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.unimag.pulsepass.persistence.dto.response.ArtistResponse;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.security.WebMvcSecurityTest;
import edu.unimag.pulsepass.persistence.service.ArtistService;

@WebMvcTest(ArtistController.class)
@WebMvcSecurityTest 
class ArtistControllerTest {

    @Autowired 
    private MockMvc mockMvc; 

    @MockitoBean 
    private ArtistService service; 

    private ArtistResponse buildArtistResponse() {
        return new ArtistResponse(1L, "Solar Beat", "Colombia", 
                                    "Electronic", true); 
    }

    // TEST-CTRL-ART-001: find by id -> 200
    @Test 
    void shouldFindArtistById() throws Exception {
        when(service.findById(1L)).thenReturn(buildArtistResponse()); 

        mockMvc.perform(get("/api/artists/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"))
                .andExpect(jsonPath("$.active").value(true)); 
        
        verify(service).findById(1L); 
    }

    // TEST-CTRL-ART-002: non-existing id -> 404
    @Test 
    void shouldReturn404WhenArtistNotFound() throws Exception {
        when(service.findById(99L))
            .thenThrow(new ResourceNotFoundException("Artist not found: 99")); 

        mockMvc.perform(get("/api/artists/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Artist not found: 99")); 

        verify(service).findById(99L); 
    }

    // TEST-CTRL-ART-003: find by stage name -> 200
    @Test
    void shouldFindArtistByStageName() throws Exception {
        when(service.findByStageName("Solar Beat")).thenReturn(buildArtistResponse()); 

        mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"))
                .andExpect(jsonPath("$.genre").value("Electronic")); 
        
        verify(service).findByStageName("Solar Beat"); 
    }

    @Test 
    void shouldFindActiveArtist() throws Exception {
        when(service.findActiveArtists()).thenReturn(List.of(buildArtistResponse())); 

        mockMvc.perform(get("/api/artists/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].stageName").value("Solar Beat"))
                .andExpect(jsonPath("$[0].genre").value("Electronic")); 
            
        verify(service).findActiveArtists(); 
    }
}
