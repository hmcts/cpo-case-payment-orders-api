package uk.gov.hmcts.reform.cpo.validators.annotation;

import uk.gov.hmcts.reform.cpo.validators.CaseIdsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = CaseIdsValidator.class)
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface ValidCaseId {

    String message() default "Error messages in case validator, the constraint is violated";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
