package edu.unimag.pulsepass.persistence.exception;

// El recurso existe, pero la operación no es válida 
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

}