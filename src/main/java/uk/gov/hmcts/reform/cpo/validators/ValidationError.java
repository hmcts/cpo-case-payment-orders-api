package uk.gov.hmcts.reform.cpo.validators;

/**
 * Validation error messages.
 **/
public final class ValidationError {

    public static final String CASE_ID_INVALID = "Case ID has to be a valid 16-digit Luhn number";
    public static final String ID_INVALID = "ID is invalid.";

    // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    private ValidationError() {
    }
}
