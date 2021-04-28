package uk.gov.hmcts.reform.cpo.auditlog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on the endpoint method to create the audit log entry and send to stdout.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface LogAudit {

    AuditOperationType operationType();

    /**
     * Expression to parse when loading the Case Payment Order ID from a string value.
     */
    String cpoId() default "";

    /**
     * Expression to parse when loading a list of Case Payment Order IDs.
     */
    String cpoIds() default "";

    /**
     * Expression to parse when loading the Case ID from a string value.
     */
    String caseId() default "";

    /**
     * Expression to parse when loading a list of Case IDs.
     */
    String caseIds() default "";

}
