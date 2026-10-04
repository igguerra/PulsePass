package edu.unimag.pulsepass.persistence.mapper;

import org.mapstruct.Mapper;

import edu.unimag.pulsepass.persistence.domain.Artist;
import edu.unimag.pulsepass.persistence.dto.response.ArtistResponse;

@Mapper(componentModel = "spring")
public interface ArtistMapper {

    ArtistResponse toResponse(Artist artist);

}
