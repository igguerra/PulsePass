package edu.unimag.pulsepass.persistence.exception;

// Conflicto de unicidad
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

}