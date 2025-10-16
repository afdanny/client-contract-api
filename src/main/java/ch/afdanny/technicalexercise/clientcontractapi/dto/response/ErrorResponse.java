package ch.afdanny.technicalexercise.clientcontractapi.dto.response;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message
) {}