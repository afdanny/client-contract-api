package ch.afdanny.technicalexercise.clientcontractapi.model;

import ch.afdanny.technicalexercise.clientcontractapi.dto.request.ContractRequest;
import ch.afdanny.technicalexercise.clientcontractapi.dto.request.ContractUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContractValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createRequest_valid_ok() {
        ContractRequest req = new ContractRequest(
                UUID.randomUUID(),
                LocalDate.of(2024, 1, 1),
                null, // optional
                new BigDecimal("123.45")
        );

        Set<ConstraintViolation<ContractRequest>> v = validator.validate(req);
        assertThat(v).isEmpty();
    }

    @Test
    void createRequest_clientId_isRequired() {
        ContractRequest req = new ContractRequest(
                null,                                  // invalid
                LocalDate.of(2024, 1, 1),
                null,
                new BigDecimal("10.00")
        );

        Set<ConstraintViolation<ContractRequest>> v = validator.validate(req);
        assertHasViolationOn(v, "clientId");
    }

    @Test
    void createRequest_costAmount_isRequired() {
        ContractRequest req = new ContractRequest(
                UUID.randomUUID(),
                LocalDate.of(2024, 1, 1),
                null,
                null                                   // invalid
        );

        Set<ConstraintViolation<ContractRequest>> v = validator.validate(req);
        assertHasViolationOn(v, "costAmount");
    }

    @Test
    void createRequest_costAmount_mustBePositive() {
        ContractRequest req = new ContractRequest(
                UUID.randomUUID(),
                LocalDate.of(2024, 1, 1),
                null,
                new BigDecimal("-1.00")               // invalid
        );

        Set<ConstraintViolation<ContractRequest>> v = validator.validate(req);
        assertHasViolationOn(v, "costAmount");
    }

    @Test
    void updateRequest_allFieldsNull_isValid() {
        // endDate et costAmount sont optionnels (nullable)
        ContractUpdateRequest req = new ContractUpdateRequest(null);

        Set<ConstraintViolation<ContractUpdateRequest>> v = validator.validate(req);
        assertThat(v).isEmpty();
    }

    @Test
    void updateRequest_costAmount_ifPresent_mustBePositive() {
        ContractUpdateRequest req = new ContractUpdateRequest(
                new BigDecimal("-0.01")               // invalid
        );

        Set<ConstraintViolation<ContractUpdateRequest>> v = validator.validate(req);
        assertHasViolationOn(v, "costAmount");
    }

    private static <T> void assertHasViolationOn(Set<ConstraintViolation<T>> violations, String property) {
        assertThat(violations)
                .as("Expected a violation on property '%s' but got: %s", property, violations)
                .anySatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo(property));
    }
}