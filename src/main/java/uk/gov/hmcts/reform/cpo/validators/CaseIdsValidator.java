package uk.gov.hmcts.reform.cpo.validators;

import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCaseId;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CaseIdsValidator implements ConstraintValidator<ValidCaseId, Optional<List<String>>>, Validator<String> {

    @Override
    public boolean isValid(final Optional<List<String>> caseIds, final ConstraintValidatorContext context) {
        if (caseIds.isEmpty()) {
            return true;
        }
        final List<String> errors = new ArrayList<>();
        caseIds.get().forEach(caseId -> validate(caseId, errors));
        if (errors.isEmpty()) {
            return true;
        }
        buildErrors(context, "These caseIDs: ", errors);
        return false;
    }

    @Override
    public void validate(final String caseId, List<String> errors) {
        if (!isValidCaseId(caseId)) {
            errors.add(caseId);
        }
    }

}
