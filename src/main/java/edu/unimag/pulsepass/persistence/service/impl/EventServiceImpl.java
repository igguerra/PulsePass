package edu.unimag.pulsepass.persistence.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.Artist;
import edu.unimag.pulsepass.persistence.domain.Event;
import edu.unimag.pulsepass.persistence.domain.EventStatus;
import edu.unimag.pulsepass.persistence.domain.Venue;
import edu.unimag.pulsepass.persistence.dto.request.CreateEventRequest;
import edu.unimag.pulsepass.persistence.dto.response.EventResponse;
import edu.unimag.pulsepass.persistence.dto.response.EventSummaryResponse;
import edu.unimag.pulsepass.persistence.exception.BusinessRuleException;
import edu.unimag.pulsepass.persistence.exception.DuplicateResourceException;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.mapper.EventMapper;
import edu.unimag.pulsepass.persistence.repository.ArtistRepository;
import edu.unimag.pulsepass.persistence.repository.EventRepository;
import edu.unimag.pulsepass.persistence.repository.VenueRepository;
import edu.unimag.pulsepass.persistence.service.EventService;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(
            EventRepository eventRepository,
            VenueRepository venueRepository,
            ArtistRepository artistRepository,
            EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.artistRepository = artistRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request) {
        // BR-EVENT-004: la fecha debe ser futura
        if (request.eventDate() == null || !request.eventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Event date must be in the future.");
        }

        // BR-EVENT-006: minimumAge >= 0 (0 o null = sin restricción de edad)
        int minimumAge = request.minimumAge() == null ? 0 : request.minimumAge();
        if (minimumAge < 0) {
            throw new BusinessRuleException("Minimum age cannot be negative.");
        }

        // BR-EVENT-001: código único
        if (eventRepository.existsByEventCode(request.eventCode())) {
            throw new DuplicateResourceException("Event code already exists: " + request.eventCode());
        }

        // BR-EVENT-002: el venue debe existir
        Venue venue = venueRepository.findByCode(request.venueCode())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + request.venueCode()));

        // BR-EVENT-003: el venue debe estar activo
        if (!venue.isActive()) {
            throw new BusinessRuleException("Venue is not active: " + venue.getCode());
        }

        // BR-EVENT-005: todo evento nuevo inicia en DRAFT, el request no controla el estado
        Event event = new Event(
                request.eventCode(),
                request.name(),
                request.category(),
                EventStatus.DRAFT,
                request.eventDate(),
                venue);
        event.setDescription(request.description());
        event.setMinimumAge(minimumAge);

        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse findByCode(String eventCode) {
        return eventMapper.toResponse(findEventOrThrow(eventCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> findPublishedEvents() {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public EventResponse publish(String eventCode) {
        Event event = findEventOrThrow(eventCode);

        // BR-EVENT-007: solo DRAFT -> PUBLISHED
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only DRAFT events can be published. Current status: " + event.getStatus());
        }

        // BR-EVENT-008: el evento debe seguir teniendo fecha futura
        if (!event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot publish an event whose date has already passed.");
        }

        // BR-EVENT-009: el venue debe continuar activo
        if (!event.getVenue().isActive()) {
            throw new BusinessRuleException("Cannot publish an event in an inactive venue.");
        }

        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EventResponse addArtist(String eventCode, Long artistId) {
        Event event = findEventOrThrow(eventCode);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + artistId));

        // BR-EVENT-011: no se agregan artistas a eventos CANCELLED o FINISHED
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED) {
            throw new BusinessRuleException(
                    "Cannot add artists to an event with status " + event.getStatus());
        }

        // BR-EVENT-010: no se asocia dos veces el mismo artista
        if (event.getArtists().contains(artist)) {
            throw new BusinessRuleException(
                    "Artist is already associated with the event: " + artist.getStageName());
        }

        event.addArtist(artist);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> findByArtist(String stageName) {
        return eventRepository.findEventsByArtist(stageName)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    private Event findEventOrThrow(String eventCode) {
        return eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventCode));
    }

}
