package ch.afdanny.technicalexercise.clientcontractapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ActiveContractsSumResponse(
        UUID clientId,
        BigDecimal totalCostAmount
) {}