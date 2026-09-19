package edu.unimag.pulsepass.persistence.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "venues")
public class Venue {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; 

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code; 

    @Column(name = "name", nullable = false, length = 150)
    private String name; 

    @Column(name = "city", nullable = false, length = 100)
    private String city; 

    @Column(name = "address", nullable = false, length = 255)
    private String address; 

    @Column(name = "capacity", nullable = false)
    private int capacity;
    
    @Column(name = "active", nullable = false)
    private boolean active = true; 

    protected Venue() {

    }

    public Venue(
        String code, 
        String name, 
        String city, 
        String address, 
        int capacity ) {

            this.code = code; 
            this.name = name; 
            this.city = city; 
            this.address = address; 
            this.capacity = capacity; 

    }


    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public int getCapacity() { return capacity; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setAddress(String address) { this.address = address; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setActive(boolean active) { this.active = active; }

}
