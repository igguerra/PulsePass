package edu.unimag.pulsepass.persistence.mapper;

import org.mapstruct.Mapper;

import edu.unimag.pulsepass.persistence.domain.Venue;
import edu.unimag.pulsepass.persistence.dto.response.VenueResponse;

@Mapper(componentModel = "spring")
public interface VenueMapper {

    VenueResponse toResponse(Venue venue);

}
