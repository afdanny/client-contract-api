package ch.afdanny.technicalexercise.clientcontractapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@Pattern(regexp = "^\\+?[0-9]{8,15}$") // International E.164-like (optional +, 8–15 digits)
public @interface Phone {
    String message() default "Phone number must be in international format (e.g. +41791234567)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}