package uk.gov.hmcts.reform.cpo.validators;

import javax.validation.ConstraintValidatorContext;
import java.util.List;

interface Validator<T> {


    default void buildErrors(final ConstraintValidatorContext context, final String message, List<String> errors) {
        final String errorsCommaSeparated = String.join(",", errors);
        context.buildConstraintViolationWithTemplate(message + errorsCommaSeparated + " are incorrect.")
            .addConstraintViolation();

    }

    void validate(T value, List<String> errors);
}
