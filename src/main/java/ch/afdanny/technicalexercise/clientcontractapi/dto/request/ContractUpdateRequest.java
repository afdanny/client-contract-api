package ch.afdanny.technicalexercise.clientcontractapi.dto.request;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ContractUpdateRequest(
        @Positive BigDecimal costAmount
) {}