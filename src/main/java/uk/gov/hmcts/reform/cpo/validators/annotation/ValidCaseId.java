package uk.gov.hmcts.reform.cpo.validators.annotation;

import uk.gov.hmcts.reform.cpo.validators.CaseIdValidator;
import uk.gov.hmcts.reform.cpo.validators.CaseIdsValidator;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {CaseIdValidator.class, CaseIdsValidator.class})
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface ValidCaseId {

    String message() default ValidationError.CASE_ID_INVALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
