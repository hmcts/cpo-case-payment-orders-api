package uk.gov.hmcts.reform.cpo.validators;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

interface Validator <T> {

    List<String> errors = new ArrayList<>();

    default void buildErrors(final ConstraintValidatorContext context, final String message) {
        final String errorsCommaSeparated = String.join(",", errors);
        context.buildConstraintViolationWithTemplate(message + errorsCommaSeparated + " are incorrect.")
            .addConstraintViolation();

    }

    void validate(T value);
}
