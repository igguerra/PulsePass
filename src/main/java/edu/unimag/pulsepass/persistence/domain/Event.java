package edu.unimag.pulsepass.persistence.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity 
@Table(name = "events")
public class Event {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "event_code", nullable = false, unique = true, length = 30)
    private String eventCode; 

    @Column(name = "name", nullable = false, length = 200)
    private String name; 

    @Column(name = "description")
    private String description; 

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private EventCategory category; 

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EventStatus status; 

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate; 

    @Column(name = "minimum_age", nullable = false)
    private int minimumAge; 

    @Column(name = "streaming_url", length = 500)
    private String streamingUrl; 

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue; 

    @ManyToMany 
    @JoinTable( 
                name = "event_artists", 
                joinColumns = @JoinColumn(name = "event_id"),
                inverseJoinColumns = @JoinColumn(name = "artist_id"))
        
        private Set<Artist> artists = new HashSet<>(); 

    protected Event() {

    }

    public Event(
        String eventCode, 
        String name,  
        EventCategory category, 
        EventStatus status, 
        LocalDateTime eventDate, 
        Venue venue){

            this.eventCode = eventCode; 
            this.name = name; 
            this.category = category; 
            this.status = status; 
            this.eventDate = eventDate; 
            this.venue = venue;  

    }

    public Long getId() { return id; }
    public String getEventCode() { return eventCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EventCategory getCategory() { return category; }
    public EventStatus getStatus() { return status; }
    public LocalDateTime getEventDate() { return eventDate; }
    public int getMinimumAge() { return minimumAge; }
    public String getStreamingUrl() { return streamingUrl; }
    public Venue getVenue() { return venue; }
    public Set<Artist> getArtists() { return artists; }
    public void addArtist(Artist artist) { this.artists.add(artist); }
    public void removeArtist(Artist artist) { this.artists.remove(artist); }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(EventCategory category) { this.category = category; }
    public void setStatus(EventStatus status) { this.status = status; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public void setMinimumAge(int minimumAge) { this.minimumAge = minimumAge; }
    public void setStreamingUrl(String streamingUrl) { this.streamingUrl = streamingUrl; }
    public void setVenue(Venue venue) { this.venue = venue; }
}
