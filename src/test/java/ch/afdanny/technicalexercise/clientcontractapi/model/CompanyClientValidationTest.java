package ch.afdanny.technicalexercise.clientcontractapi.model;

import jakarta.validation.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for validation constraints on CompanyClient entity.
 * This test only checks Bean Validation annotations (no database required).
 */
class CompanyClientValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void companyIdentifier_mustMatchExpectedPattern() {
        CompanyClient invalid = CompanyClient.builder()
                .name("Vaudoise Assurances")
                .email("info@vaudoise.ch")
                .phone("+41219999999")
                .companyIdentifier("123-ABC") // ❌ invalid format
                .build();

        Set<ConstraintViolation<CompanyClient>> violations = validator.validate(invalid);

        // Expect one violation on companyIdentifier field
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("companyIdentifier"));
    }

    @Test
    void companyIdentifier_isRequired() {
        CompanyClient invalid = CompanyClient.builder()
                .name("Vaudoise")
                .email("info@vaudoise.ch")
                .phone("+41210000000")
                .companyIdentifier(null) // ❌ required
                .build();

        Set<ConstraintViolation<CompanyClient>> violations = validator.validate(invalid);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("companyIdentifier"));
    }

    @Test
    void email_mustBeValid() {
        CompanyClient invalid = CompanyClient.builder()
                .name("Vaudoise")
                .email("not-an-email") // ❌ invalid email
                .phone("+41210000000")
                .companyIdentifier("AAA-123")
                .build();

        Set<ConstraintViolation<CompanyClient>> violations = validator.validate(invalid);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void phone_mustMatchPattern() {
        CompanyClient invalid = CompanyClient.builder()
                .name("Vaudoise")
                .email("info@vaudoise.ch")
                .phone("abcdef") // ❌ invalid format
                .companyIdentifier("AAA-123")
                .build();

        Set<ConstraintViolation<CompanyClient>> violations = validator.validate(invalid);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("phone"));
    }

    @Test
    void validCompanyClient_shouldHaveNoViolations() {
        CompanyClient valid = CompanyClient.builder()
                .name("Vaudoise Assurances SA")
                .email("contact@vaudoise.ch")
                .phone("+41216188111")
                .companyIdentifier("ABC-456")
                .build();

        Set<ConstraintViolation<CompanyClient>> violations = validator.validate(valid);

        assertThat(violations).isEmpty();
    }
}