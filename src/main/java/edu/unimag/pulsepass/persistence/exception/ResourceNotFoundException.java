package edu.unimag.pulsepass.persistence.exception;

// El recurso solicitado no existe 
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}