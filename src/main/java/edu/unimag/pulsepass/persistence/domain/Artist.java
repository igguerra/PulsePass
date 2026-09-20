package edu.unimag.pulsepass.persistence.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "Artists")
public class Artist {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "stage_name", nullable = false, unique = true, length = 150)
    private String stageName; 

    @Column(name = "country", length = 100)
    private String country; 

    @Column(name = "genre", length = 100)
    private String genre; 

    @Column(name = "active", nullable = false)
    private boolean active = true; 

    protected  Artist() {

    }

    public Artist(
        String stageName, 
        String country, 
        String genre) {

            this.stageName = stageName; 
            this.country = country; 
            this.genre = genre; 

    }

    
    public Long getId() { return id; }
    public String getStageName() { return stageName; }
    public String getCountry() { return country; }
    public String getGenre() { return genre; }
    public boolean isActive() { return active; }

    public void setCountry(String country) { this.country = country; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setActive(boolean active) { this.active = active; }


    @Override 
    public boolean equals(Object o) {
        if (this == o) {
            return true; 
        }

        if (!(o instanceof Artist other)) {
            return true; 
        }else{
            return stageName != null && stageName.equals(other.getStageName());
        }
    }

    @Override 
    public int hashCode(){
        return Objects.hash(stageName); 
    }
}
