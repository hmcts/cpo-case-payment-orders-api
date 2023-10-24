package uk.gov.hmcts.reform.cpo.validators;

import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IdsValidator implements ConstraintValidator<ValidCpoId, Optional<List<String>>>, Validator<String> {

    @Override
    public boolean isValid(final Optional<List<String>> cpoIds, final ConstraintValidatorContext context) {
        if (cpoIds.isEmpty()) {
            return true;
        }
        final List<String> errors = new ArrayList<>();
        cpoIds.get().forEach(cpoId -> validate(cpoId, errors));
        if (errors.isEmpty()) {
            return true;
        }
        buildErrors(context, "These ids: ", errors);
        return false;
    }

    @Override
    public void validate(final String cpoId, List<String> errors) {
        if (!isValidCpoId(cpoId)) {
            errors.add(cpoId);
        }
    }

}
