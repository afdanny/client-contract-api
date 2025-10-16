package ch.afdanny.technicalexercise.clientcontractapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        UUID clientId,
        LocalDate startDate,
        LocalDate endDate,      // null => active/open
        BigDecimal costAmount
) { }