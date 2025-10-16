package ch.afdanny.technicalexercise.clientcontractapi.dto.request;

import ch.afdanny.technicalexercise.clientcontractapi.validation.Phone;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreatePersonClientRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Phone String phone,
        @NotNull @Past LocalDate birthdate
) {}