package uk.gov.hmcts.reform.cpo.validators;

import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IdsValidator implements ConstraintValidator<ValidCpoId, Optional<List<String>>>, Validator <String>{

    private final List<String> errors = new ArrayList<>();

    @Override
    public void initialize(final ValidCpoId constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Optional<List<String>> casesId, final ConstraintValidatorContext context) {
        if (!casesId.isPresent()) {
            return true;
        }
        casesId.get().stream().forEach(caseId -> validate(caseId));
        if (errors.isEmpty()) {
            return true;
        }
        buildErrors(context, "These ids: ");
        return false;
    }

    @Override
    public void validate(final String caseId) {
        try {
            UUID.fromString(caseId);
        } catch (Exception exception) {
            errors.add(caseId);
        }
    }
}
