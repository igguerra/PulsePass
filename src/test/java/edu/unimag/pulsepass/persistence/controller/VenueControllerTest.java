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

import edu.unimag.pulsepass.persistence.dto.response.VenueResponse;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.security.WebMvcSecurityTest;
import edu.unimag.pulsepass.persistence.service.VenueService;

@WebMvcTest(VenueController.class)
@WebMvcSecurityTest 
class VenueControllerTest {

    @Autowired 
    private MockMvc mockMvc; 

    @MockitoBean 
    private VenueService service; 

    private VenueResponse buildVenueResponse(){
        return new VenueResponse(
            1L, "VEN-SMR-01", "Marina Convention Center", "Santa Marta", 
            "Calle 22 # 1-50", 3, true); 
    }

    // TEST-CTRL-VEN-001: existing venue -> 200
    @Test 
    void shouldFindVenueCode() throws Exception {
        when(service.findByCode("VEN-SMR-01")).thenReturn(buildVenueResponse()); 

        mockMvc.perform(get("/api/venues/VEN-SMR-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.name").value("Marina Convention Center"))
                .andExpect(jsonPath("$.city").value("Santa Marta"))
                .andExpect(jsonPath("$.capacity").value(3))
                .andExpect(jsonPath("$.active").value(true)); 

        verify(service).findByCode("VEN-SMR-01"); 
    }

    // TEST-CTRL-VEN-002: non-existing venue -> 404
    @Test 
    void shouldReturn404WhenVenueNotFound() throws Exception {
        when(service.findByCode("NOPE-00"))
            .thenThrow(new ResourceNotFoundException("Venue not found: NOPE-00")); 

        mockMvc.perform(get("/api/venues/NOPE-00"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Venue not found: NOPE-00")); 
        
        verify(service).findByCode("NOPE-00"); 
    }

    // TEST-CTRL-VEN-003: active venues -> 200
    @Test 
    void shouldFindActiveVenues() throws Exception {
        when(service.findActiveVenues()).thenReturn(List.of(buildVenueResponse()));
        
        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$[0].active").value(true)); 
        
        verify(service).findActiveVenues(); 
    }

}
