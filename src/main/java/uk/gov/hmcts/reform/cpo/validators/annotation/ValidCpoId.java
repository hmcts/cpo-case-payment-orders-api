package uk.gov.hmcts.reform.cpo.validators.annotation;

import uk.gov.hmcts.reform.cpo.validators.IdValidator;
import uk.gov.hmcts.reform.cpo.validators.IdsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {IdValidator.class, IdsValidator.class})
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface ValidCpoId {

    String message() default "Error messages in id validator, the constraint is violated";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
