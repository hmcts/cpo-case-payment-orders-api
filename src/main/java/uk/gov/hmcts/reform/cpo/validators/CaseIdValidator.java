package uk.gov.hmcts.reform.cpo.validators;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCaseId;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class CaseIdValidator implements ConstraintValidator<ValidCaseId, String>, Validator<String> {

    @Override
    public boolean isValid(final String caseId, final ConstraintValidatorContext context) {
        if (StringUtils.isBlank(caseId)) {
            return true; // NB: is required checked elsewhere
        }

        final List<String> errors = new ArrayList<>();
        validate(caseId, errors);
        if (errors.isEmpty()) {
            return true;
        }
        context.buildConstraintViolationWithTemplate(String.join(", ", errors)).addConstraintViolation();
        context.disableDefaultConstraintViolation();
        return false;
    }

    @Override
    public void validate(final String caseId, List<String> errors) {
        if (!isValidCaseId(caseId)) {
            errors.add(ValidationError.CASE_ID_INVALID);
        }
    }

}
