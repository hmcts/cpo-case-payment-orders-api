package uk.gov.hmcts.reform.cpo.validators;

/**
 * Validation error messages.
 **/
public final class ValidationError {

    public static final String ACTION_REQUIRED = "Action is required.";
    public static final String CASE_ID_INVALID = "Case ID has to be a valid 16-digit Luhn number.";
    public static final String CASE_ID_INVALID_LENGTH = "Case ID has to be 16-digits long.";
    public static final String CASE_ID_REQUIRED = "Case ID is required.";
    public static final String EFFECTIVE_FROM_REQUIRED = "Effective From is required.";
    public static final String ID_INVALID = "ID is invalid.";
    public static final String ID_REQUIRED = "ID is required.";
    public static final String ORDER_REFERENCE_REQUIRED = "Order Reference is required.";
    public static final String RESPONSIBLE_PARTY_REQUIRED = "Responsible Party is required.";
    public static final String ORDER_REFERENCE_INVALID = "Order Reference has invalid format.";


    // additional constraints etc..
    public static final String CPO_NOT_FOUND = "Case Payment Order does not exist.";
    public static final String CASE_ID_ORDER_REFERENCE_UNIQUE = "Case ID and Order Reference pairing must be unique.";
    public static final String IDAM_ID_NOT_FOUND = "Idam ID cannot be retrieved";
    public static final String ARGUMENT_NOT_VALID = "Input not valid";

    // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    private ValidationError() {
    }
}
