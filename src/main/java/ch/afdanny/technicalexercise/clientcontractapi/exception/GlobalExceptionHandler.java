package ch.afdanny.technicalexercise.clientcontractapi.exception;

import ch.afdanny.technicalexercise.clientcontractapi.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static ErrorResponse body(HttpStatus status, String message) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleContractNotFound(NotFoundException ex) {
        var status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(body(status, ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleContractConflict(ConflictException ex) {
        var status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(body(status, ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleContractBadRequest(BadRequestException ex) {
        var status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(body(status, ex.getMessage()));
    }
}