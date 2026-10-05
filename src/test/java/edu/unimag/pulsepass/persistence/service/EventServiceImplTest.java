package edu.unimag.pulsepass.persistence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.unimag.pulsepass.persistence.domain.Artist;
import edu.unimag.pulsepass.persistence.domain.Event;
import edu.unimag.pulsepass.persistence.domain.EventCategory;
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
import edu.unimag.pulsepass.persistence.service.impl.EventServiceImpl;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    private static final String EVENT_CODE = "CMF-2026";
    private static final String VENUE_CODE = "VEN-SMR-01";
    private static final LocalDateTime FUTURE = LocalDateTime.now().plusMonths(3);
    private static final LocalDateTime PAST = LocalDateTime.now().minusDays(1);

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    // ---------- helpers ----------

    private Venue venue() {
        return new Venue(VENUE_CODE, "Marina Convention Center", "Santa Marta", "Carrera 1 # 1-1", 3);
    }

    private Event event(EventStatus status, LocalDateTime date) {
        Event event = new Event(EVENT_CODE, "Caribbean Music Fest 2026",
                EventCategory.MUSIC, status, date, venue());
        event.setMinimumAge(18);
        return event;
    }

    private CreateEventRequest request(LocalDateTime date, Integer minimumAge) {
        return new CreateEventRequest(EVENT_CODE, "Caribbean Music Fest 2026",
                "Festival de música del Caribe", EventCategory.MUSIC, date, minimumAge, VENUE_CODE);
    }

    private EventResponse response(EventStatus status) {
        return new EventResponse(1L, EVENT_CODE, "Caribbean Music Fest 2026",
                "Festival de música del Caribe", EventCategory.MUSIC, status, FUTURE, 18,
                VENUE_CODE, "Marina Convention Center", List.of());
    }

    // ---------- findByCode ----------

    @Test
    void findByCode_existingEvent_returnsDto() { // TEST-EVENT-001
        // ARRANGE
        Event event = event(EventStatus.PUBLISHED, FUTURE);
        EventResponse expected = response(EventStatus.PUBLISHED);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(expected);

        // ACT
        EventResponse result = eventService.findByCode(EVENT_CODE);

        // ASSERT
        assertThat(result).isEqualTo(expected);
        verify(eventRepository).findByEventCode(eq(EVENT_CODE));
    }

    @Test
    void findByCode_missingEvent_throwsResourceNotFound() { // TEST-EVENT-002
        // ARRANGE
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.findByCode(EVENT_CODE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EVENT_CODE);
        verify(eventMapper, never()).toResponse(any(Event.class));
    }

    // ---------- create ----------

    @Test
    void create_validRequest_savesEventAsDraft() { // TEST-EVENT-003
        // ARRANGE
        Venue venue = venue();
        EventResponse expected = response(EventStatus.DRAFT);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventMapper.toResponse(any(Event.class))).thenReturn(expected);

        // ACT
        EventResponse result = eventService.create(request(FUTURE, 18));

        // ASSERT
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event saved = captor.getValue();
        assertThat(saved.getEventCode()).isEqualTo(EVENT_CODE);
        assertThat(saved.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(saved.getMinimumAge()).isEqualTo(18);
        assertThat(saved.getVenue()).isSameAs(venue);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void create_missingVenue_throwsResourceNotFoundAndNeverSaves() { // TEST-EVENT-004
        // ARRANGE
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(request(FUTURE, 18)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(VENUE_CODE);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void create_inactiveVenue_throwsBusinessRule() { // TEST-EVENT-005
        // ARRANGE
        Venue inactiveVenue = venue();
        inactiveVenue.setActive(false);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(inactiveVenue));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(request(FUTURE, 18)))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void create_pastDate_throwsBusinessRule() { // TEST-EVENT-006
        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(request(PAST, 18)))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void create_negativeMinimumAge_throwsBusinessRule() { // BR-EVENT-006
        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(request(FUTURE, -1)))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void create_duplicateCode_throwsDuplicateResource() { // BR-EVENT-001
        // ARRANGE
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.create(request(FUTURE, 18)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(venueRepository, never()).findByCode(any());
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ---------- publish ----------

    @Test
    void publish_validDraft_changesStatusToPublished() { // TEST-EVENT-007
        // ARRANGE
        Event event = event(EventStatus.DRAFT, FUTURE);
        EventResponse expected = response(EventStatus.PUBLISHED);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(expected);

        // ACT
        EventResponse result = eventService.publish(EVENT_CODE);

        // ASSERT
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(result.status()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(event);
    }

    @Test
    void publish_cancelledEvent_throwsBusinessRuleAndNeverSaves() { // TEST-EVENT-008
        // ARRANGE
        Event event = event(EventStatus.CANCELLED, FUTURE);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.publish(EVENT_CODE))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void publish_pastDate_throwsBusinessRuleAndNeverSaves() { // BR-EVENT-008
        // ARRANGE
        Event event = event(EventStatus.DRAFT, PAST);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.publish(EVENT_CODE))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void publish_inactiveVenue_throwsBusinessRuleAndNeverSaves() { // BR-EVENT-009
        // ARRANGE
        Event event = event(EventStatus.DRAFT, FUTURE);
        event.getVenue().setActive(false);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.publish(EVENT_CODE))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ---------- addArtist ----------

    @Test
    void addArtist_validEventAndArtist_associatesAndSaves() { // FR-SVC-007
        // ARRANGE
        Event event = event(EventStatus.DRAFT, FUTURE);
        Artist artist = new Artist("Solar Beat", "Colombia", "Electronic");
        EventResponse expected = response(EventStatus.DRAFT);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(expected);

        // ACT
        EventResponse result = eventService.addArtist(EVENT_CODE, 1L);

        // ASSERT
        assertThat(event.getArtists()).containsExactly(artist);
        assertThat(result).isEqualTo(expected);
        verify(eventRepository).save(event);
    }

    @Test
    void addArtist_alreadyAssociated_throwsBusinessRuleAndNeverSaves() { // BR-EVENT-010
        // ARRANGE
        Event event = event(EventStatus.DRAFT, FUTURE);
        Artist artist = new Artist("Solar Beat", "Colombia", "Electronic");
        event.addArtist(artist);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.addArtist(EVENT_CODE, 1L))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(event.getArtists()).hasSize(1);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void addArtist_cancelledEvent_throwsBusinessRuleAndNeverSaves() { // BR-EVENT-011
        // ARRANGE
        Event event = event(EventStatus.CANCELLED, FUTURE);
        Artist artist = new Artist("Neon Waves", "Colombia", "Pop");
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(artistRepository.findById(2L)).thenReturn(Optional.of(artist));

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.addArtist(EVENT_CODE, 2L))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(event.getArtists()).isEmpty();
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void addArtist_missingArtist_throwsResourceNotFound() {
        // ARRANGE
        Event event = event(EventStatus.DRAFT, FUTURE);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> eventService.addArtist(EVENT_CODE, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ---------- listados ----------

    @Test
    void findPublishedEvents_returnsSummaries() { // FR-SVC-005
        // ARRANGE
        Event event = event(EventStatus.PUBLISHED, FUTURE);
        EventSummaryResponse summary = new EventSummaryResponse(1L, EVENT_CODE,
                "Caribbean Music Fest 2026", EventCategory.MUSIC, EventStatus.PUBLISHED,
                FUTURE, "Marina Convention Center");
        when(eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED))
                .thenReturn(List.of(event));
        when(eventMapper.toSummary(event)).thenReturn(summary);

        // ACT
        List<EventSummaryResponse> result = eventService.findPublishedEvents();

        // ASSERT
        assertThat(result).containsExactly(summary);
        verify(eventRepository).findByStatusOrderByEventDateAsc(eq(EventStatus.PUBLISHED));
    }

}
