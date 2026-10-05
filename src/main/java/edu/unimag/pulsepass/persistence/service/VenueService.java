package edu.unimag.pulsepass.persistence.service;

import java.util.List;

import edu.unimag.pulsepass.persistence.dto.response.VenueResponse;

public interface VenueService {

    VenueResponse findByCode(String code);

    List<VenueResponse> findActiveVenues();

}
