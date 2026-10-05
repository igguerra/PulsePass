package edu.unimag.pulsepass.persistence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.unimag.pulsepass.persistence.domain.Venue;
import edu.unimag.pulsepass.persistence.dto.response.VenueResponse;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.mapper.VenueMapper;
import edu.unimag.pulsepass.persistence.repository.VenueRepository;
import edu.unimag.pulsepass.persistence.service.impl.VenueServiceImpl;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    private static final String VENUE_CODE = "VEN-SMR-01";

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private VenueMapper venueMapper;

    @InjectMocks
    private VenueServiceImpl venueService;

    private Venue venue() {
        return new Venue(VENUE_CODE, "Marina Convention Center", "Santa Marta", "Carrera 1 # 1-1", 3);
    }

    private VenueResponse response() {
        return new VenueResponse(1L, VENUE_CODE, "Marina Convention Center", "Santa Marta",
                "Carrera 1 # 1-1", 3, true);
    }

    @Test
    void findByCode_existingVenue_returnsDto() { // FR-SVC-001
        // ARRANGE
        Venue venue = venue();
        VenueResponse expected = response();
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(expected);

        // ACT
        VenueResponse result = venueService.findByCode(VENUE_CODE);

        // ASSERT
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findByCode_missingVenue_throwsResourceNotFound() { // BR-VENUE-001
        // ARRANGE
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> venueService.findByCode(VENUE_CODE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(VENUE_CODE);
        verify(venueMapper, never()).toResponse(any(Venue.class));
    }

    @Test
    void findActiveVenues_returnsOnlyWhatTheActiveQueryReturns() { // BR-VENUE-002
        // ARRANGE
        Venue venue = venue();
        VenueResponse expected = response();
        when(venueRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(expected);

        // ACT
        List<VenueResponse> result = venueService.findActiveVenues();

        // ASSERT
        assertThat(result).containsExactly(expected);
        verify(venueRepository).findByActiveTrueOrderByNameAsc();
    }

}
