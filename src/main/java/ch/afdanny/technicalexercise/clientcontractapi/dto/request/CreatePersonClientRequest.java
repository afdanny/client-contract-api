package ch.afdanny.technicalexercise.clientcontractapi.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreatePersonClientRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotNull @Past LocalDate birthdate
) {}