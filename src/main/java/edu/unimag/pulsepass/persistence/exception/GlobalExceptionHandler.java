package edu.unimag.pulsepass.persistence.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import edu.unimag.pulsepass.persistence.dto.response.ErrorResponse;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, Map<String, String> details){
        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(), 
            status.value(), 
            status.getReasonPhrase(), 
            message, 
            details
        ); 

        return ResponseEntity.status(status).body(body); 
    }

    @ExceptionHandler (ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {

        return build(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of()); 

    }

    @ExceptionHandler (DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {

        return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of()); 

    }

    @ExceptionHandler (BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {

        return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of()); 
    }

    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>(); 
        ex.getBindingResult().getFieldErrors().forEach(error -> details.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return build(HttpStatus.BAD_REQUEST, "Validation Failed", details); 
    }

    @ExceptionHandler (HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequest(HttpMessageNotReadableException ex){

        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", Map.of()); 
    }

    @ExceptionHandler (Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex){

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error", Map.of()); 

    }

}
