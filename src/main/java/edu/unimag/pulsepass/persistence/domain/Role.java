package edu.unimag.pulsepass.persistence.domain;

public enum Role {

    USER, 
    STAFF, 
    ADMIN; 

    public String authority() {
        return "ROLE_" + name(); 
    }

}
