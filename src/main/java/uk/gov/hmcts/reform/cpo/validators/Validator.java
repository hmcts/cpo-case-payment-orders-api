package uk.gov.hmcts.reform.cpo.validators;


import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit.LUHN_CHECK_DIGIT;

public interface Validator<T> {

    String CASE_ID_RG = "\\d{16}";
    String ORDER_REFERENCE_RG = "^2[0-9]{3}-[0-9]{13}$";

    default void buildErrors(final ConstraintValidatorContext context, final String message, List<String> errors) {
        final var errorsCommaSeparated = String.join(",", errors);
        context.buildConstraintViolationWithTemplate(message + errorsCommaSeparated + " are incorrect.")
            .addConstraintViolation();
    }

    default boolean isValidCaseId(String caseId) {

        return caseId != null
            && caseId.matches(CASE_ID_RG)
            && LUHN_CHECK_DIGIT.isValid(caseId);
    }

    default boolean isValidCpoId(String cpoId) {
        try {
            UUID.fromString(cpoId);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    void validate(T value, List<String> errors);

}
