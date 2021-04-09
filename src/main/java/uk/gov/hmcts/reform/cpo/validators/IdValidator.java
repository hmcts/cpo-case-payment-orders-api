package uk.gov.hmcts.reform.cpo.validators;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class IdValidator implements ConstraintValidator<ValidCpoId, String>, Validator<String> {

    @Override
    public boolean isValid(final String id, final ConstraintValidatorContext context) {
        if (StringUtils.isBlank(id)) {
            return true; // NB: is required checked elsewhere
        }

        final List<String> errors = new ArrayList<>();
        validate(id, errors);
        if (errors.isEmpty()) {
            return true;
        }
        context.buildConstraintViolationWithTemplate(String.join(", ", errors)).addConstraintViolation();
        context.disableDefaultConstraintViolation();
        return false;
    }

    @Override
    public void validate(final String id, List<String> errors) {
        if (!isValidCpoId(id)) {
            errors.add(ValidationError.ID_INVALID);
        }
    }

}
