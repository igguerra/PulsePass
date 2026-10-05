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

import edu.unimag.pulsepass.persistence.domain.Artist;
import edu.unimag.pulsepass.persistence.dto.response.ArtistResponse;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.mapper.ArtistMapper;
import edu.unimag.pulsepass.persistence.repository.ArtistRepository;
import edu.unimag.pulsepass.persistence.service.impl.ArtistServiceImpl;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistMapper artistMapper;

    @InjectMocks
    private ArtistServiceImpl artistService;

    private Artist artist() {
        return new Artist("Solar Beat", "Colombia", "Electronic");
    }

    private ArtistResponse response() {
        return new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true);
    }

    @Test
    void findById_existingArtist_returnsDto() { // FR-SVC-009
        // ARRANGE
        Artist artist = artist();
        ArtistResponse expected = response();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(expected);

        // ACT
        ArtistResponse result = artistService.findById(1L);

        // ASSERT
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findById_missingArtist_throwsResourceNotFound() { // BR-ARTIST-001
        // ARRANGE
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> artistService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(artistMapper, never()).toResponse(any(Artist.class));
    }

    @Test
    void findByStageName_missingArtist_throwsResourceNotFound() { // BR-ARTIST-001
        // ARRANGE
        when(artistRepository.findByStageNameIgnoreCase("Unknown")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> artistService.findByStageName("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Unknown");
    }

    @Test
    void findActiveArtists_returnsOnlyWhatTheActiveQueryReturns() { // BR-ARTIST-002
        // ARRANGE
        Artist artist = artist();
        ArtistResponse expected = response();
        when(artistRepository.findByActiveTrueOrderByStageNameAsc()).thenReturn(List.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(expected);

        // ACT
        List<ArtistResponse> result = artistService.findActiveArtists();

        // ASSERT
        assertThat(result).containsExactly(expected);
        verify(artistRepository).findByActiveTrueOrderByStageNameAsc();
    }

}
