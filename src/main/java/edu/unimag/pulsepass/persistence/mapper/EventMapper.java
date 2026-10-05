package edu.unimag.pulsepass.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import edu.unimag.pulsepass.persistence.domain.Event;
import edu.unimag.pulsepass.persistence.dto.response.EventResponse;
import edu.unimag.pulsepass.persistence.dto.response.EventSummaryResponse;

// uses = ArtistMapper: convierte el Set<Artist> del evento en List<ArtistResponse>.
@Mapper(componentModel = "spring", uses = ArtistMapper.class)
public interface EventMapper {

    @Mapping(target = "venueCode", source = "venue.code")
    @Mapping(target = "venueName", source = "venue.name")
    EventResponse toResponse(Event event);

    @Mapping(target = "venueName", source = "venue.name")
    EventSummaryResponse toSummary(Event event);

}
