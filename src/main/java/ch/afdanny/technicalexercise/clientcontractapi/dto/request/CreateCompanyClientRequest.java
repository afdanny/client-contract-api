package ch.afdanny.technicalexercise.clientcontractapi.dto.request;

import jakarta.validation.constraints.*;

public record CreateCompanyClientRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}-\\d{3}$", message = "Expected format like AAA-123")
        String companyIdentifier
) {}