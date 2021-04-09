package uk.gov.hmcts.reform.cpo.validators;

/**
 * Validation error messages.
 **/
public final class ValidationError {

    public static final String ACTION_REQUIRED = "Action is required.";
    public static final String CASE_ID_INVALID = "Case ID has to be a valid 16-digit Luhn number";
    public static final String CASE_ID_REQUIRED = "Case ID is required.";
    public static final String EFFECTIVE_FROM_REQUIRED = "Effective From is required.";
    public static final String ID_INVALID = "ID is invalid.";
    public static final String ID_REQUIRED = "ID is required.";
    public static final String ORDER_REFERENCE_REQUIRED = "Order Reference Party is required.";
    public static final String RESPONSIBLE_PARTY_REQUIRED = "Responsible Party is required.";

    // additional constraints etc..
    public static final String CPO_NOT_FOUND = "Case Payment Order does not exist";
    public static final String CASE_ID_ORDER_REFERENCE_UNIQUE
        = "A case payment order with the specified Order Reference already exists on the case";

    // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    private ValidationError() {
    }

}
