package ch.afdanny.technicalexercise.clientcontractapi.dto.request;

import ch.afdanny.technicalexercise.clientcontractapi.validation.Phone;
import jakarta.validation.constraints.*;

public record CreateCompanyClientRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Phone String phone,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}-\\d{3}$", message = "Expected format like aaa-123")
        String companyIdentifier
) {}