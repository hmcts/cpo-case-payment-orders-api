package uk.gov.hmcts.reform.cpo.validators;

import uk.gov.hmcts.reform.cpo.validators.annotation.IdsAnnotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//
//1) add errors in swagger
//2) aDD EXCEPTION HANDLER
//3) UPDATE THE TICKET !!!!

public class IdsValidator implements ConstraintValidator<IdsAnnotation, Optional<List<String>>>, Validator {

    private final List<String> errors = new ArrayList<>();

    @Override
    public void initialize(final IdsAnnotation constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Optional<List<String>> casesId, final ConstraintValidatorContext context) {
        if (!casesId.isPresent()) {
            return true;
        }
        casesId.get().stream().forEach(caseId -> validateId(caseId));
        if (errors.isEmpty()) {
            return true;
        }
        buildErrors(context, "These ids: ");
        return false;
    }

    private void validateId(final String caseId) {
        try {
            UUID.fromString(caseId);
        } catch (Exception exception) {
            errors.add(caseId);
        }
    }
}
