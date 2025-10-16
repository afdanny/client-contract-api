package ch.afdanny.technicalexercise.clientcontractapi.dto.request;

import jakarta.validation.constraints.*;

public record UpdateClientRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String phone
) {}