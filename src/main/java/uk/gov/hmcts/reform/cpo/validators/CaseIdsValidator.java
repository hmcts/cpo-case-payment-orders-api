package uk.gov.hmcts.reform.cpo.validators;

import uk.gov.hmcts.reform.cpo.validators.annotation.CaseIdsAnnotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CaseIdsValidator implements ConstraintValidator<CaseIdsAnnotation, Optional<List<String>>>,Validator {

    private final String NUMERIC_EXPRESSION = "\\d{16}";
    private final List<String> errors = new ArrayList<>();

    @Override
    public void initialize(final CaseIdsAnnotation constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Optional<List<String>> ids, final ConstraintValidatorContext context) {
        if (!ids.isPresent()) {
            return true;
        }
        ids.get().stream().forEach(caseId -> validateId(caseId));
        if (errors.isEmpty()) {
            return true;
        }
        buildErrors(context,"These casesIds: ");
        return false;
    }

    private void validateId(final String caseId) {
        if (!caseId.matches(NUMERIC_EXPRESSION)) {
            errors.add(caseId);
        }
    }

}
