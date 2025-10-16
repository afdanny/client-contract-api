package ch.afdanny.technicalexercise.clientcontractapi.model;

import jakarta.validation.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for validation constraints on PersonClient entity.
 * This test only checks Bean Validation annotations (no database required).
 */
class PersonClientValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void birthdate_mustBeInThePast() {
        PersonClient invalid = PersonClient.builder()
                .name("Alice")
                .email("alice@example.ch")
                .phone("+41791234567")
                .birthdate(LocalDate.now().plusDays(1)) // ❌ future date
                .build();

        Set<ConstraintViolation<PersonClient>> violations = validator.validate(invalid);

        // Expect one violation on birthdate field
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthdate"));
    }

    @Test
    void birthdate_isRequired() {
        PersonClient invalid = PersonClient.builder()
                .name("Bob")
                .email("bob@example.ch")
                .phone("+41211234567")
                .birthdate(null) // ❌ missing
                .build();

        Set<ConstraintViolation<PersonClient>> violations = validator.validate(invalid);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthdate"));
    }

    @Test
    void email_mustBeValid() {
        PersonClient invalid = PersonClient.builder()
                .name("Charlie")
                .email("not-an-email") // ❌ invalid email
                .phone("+41221111111")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<PersonClient>> violations = validator.validate(invalid);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void phone_mustFollowPattern() {
        PersonClient invalid = PersonClient.builder()
                .name("David")
                .email("david@example.ch")
                .phone("invalid") // ❌ violates pattern
                .birthdate(LocalDate.of(1985, 5, 10))
                .build();

        Set<ConstraintViolation<PersonClient>> violations = validator.validate(invalid);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("phone"));
    }

    @Test
    void name_isRequired() {
        PersonClient invalid = PersonClient.builder()
                .name("") // ❌ empty name
                .email("eva@example.ch")
                .phone("+41793333333")
                .birthdate(LocalDate.of(1995, 3, 15))
                .build();

        Set<ConstraintViolation<PersonClient>> violations = validator.validate(invalid);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void validPersonClient_shouldHaveNoViolations() {
        PersonClient valid = PersonClient.builder()
                .name("Fiona")
                .email("fiona@example.ch")
                .phone("+41789999999")
                .birthdate(LocalDate.of(1990, 4, 25))
                .build();

        Set<ConstraintViolation<PersonClient>> violations = validator.validate(valid);

        assertThat(violations).isEmpty();
    }
}