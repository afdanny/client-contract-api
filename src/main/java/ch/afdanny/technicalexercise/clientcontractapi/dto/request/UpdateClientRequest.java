package ch.afdanny.technicalexercise.clientcontractapi.dto.request;

import ch.afdanny.technicalexercise.clientcontractapi.validation.Phone;
import jakarta.validation.constraints.*;

public record UpdateClientRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Phone String phone
) {}