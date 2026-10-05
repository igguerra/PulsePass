package edu.unimag.pulsepass.persistence.service;

import java.util.List;

import edu.unimag.pulsepass.persistence.dto.response.ArtistResponse;

public interface ArtistService {

    ArtistResponse findById(Long id);

    ArtistResponse findByStageName(String stageName);

    List<ArtistResponse> findActiveArtists();

}
