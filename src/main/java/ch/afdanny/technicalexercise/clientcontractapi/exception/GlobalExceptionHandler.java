package ch.afdanny.technicalexercise.clientcontractapi.exception;

import ch.afdanny.technicalexercise.clientcontractapi.service.ClientService;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType VA_ERROR = MediaType.valueOf("application/vnd.va.error+json");
    private static final MediaType VA_VALIDATION = MediaType.valueOf("application/vnd.va.validation+json");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<Map<String, Object>>>> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        var validations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "display", fe.getDefaultMessage(),
                        "code", "validationError",
                        "fields", List.of(fe.getField())
                )).toList();

        var body = Map.of("validations", validations);
        return ResponseEntity.unprocessableEntity()
                .contentType(VA_VALIDATION)
                .body(body);
    }

    @ExceptionHandler(ClientService.ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ClientService.ConflictException ex) {
        var body = Map.of(
                "message", ex.getMessage(),
                "display", "Conflict",
                "code", "conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(VA_ERROR)
                .body(body);
    }

    @ExceptionHandler(ClientService.NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ClientService.NotFoundException ex) {
        var body = Map.of(
                "message", ex.getMessage(),
                "display", "Not Found",
                "code", "notFound"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(VA_ERROR)
                .body(body);
    }
}