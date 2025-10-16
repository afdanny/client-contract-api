package ch.afdanny.technicalexercise.clientcontractapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractRequest(
        @NotNull UUID clientId,
        LocalDate startDate,
        LocalDate endDate,
        @NotNull @Positive BigDecimal costAmount
) {}