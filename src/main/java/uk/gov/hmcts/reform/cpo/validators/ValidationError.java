package uk.gov.hmcts.reform.cpo.validators;

/**
 * Validation error messages.
 **/
public final class ValidationError {

    public static final String ACTION_REQUIRED = "Action is required.";
    public static final String CASE_ID_INVALID = "Case ID has to be a valid 16-digit Luhn number.";
    public static final String CASE_ID_REQUIRED = "Case ID is required.";
    public static final String EFFECTIVE_FROM_REQUIRED = "Effective From is required.";
    public static final String ID_INVALID = "ID is invalid.";
    public static final String ID_REQUIRED = "ID is required.";
    public static final String ORDER_REFERENCE_REQUIRED = "Order Reference Party is required.";
    public static final String RESPONSIBLE_PARTY_REQUIRED = "Responsible Party is required.";

    public static final String IDS_EMPTY = "IDs are required";
    public static final String CASE_ID_INVALID_LENGTH = "Case ID has to be 16-digits long";
    public static final String CASE_IDS_EMPTY = "Case ID can not be empty";
    public static final String CANNOT_DELETE_WITH_BOTH_ID_AND_CASE_ID_SPECIFIED =
            "Can't delete case payment order with both id AND case-id specified";
    public static final String CPO_NO_FOUND_BY_ID = "Case Payment Order specified by IDs does not exist";
    public static final String CPO_NOT_FOUND_BY_CASE_ID = "Case Payment Order specified by Case IDs does not exist";

    // additional constraints etc..
    public static final String CPO_NOT_FOUND = "Case Payment Order does not exist.";
    public static final String CASE_ID_ORDER_REFERENCE_UNIQUE = "Case ID and Order Reference pairing must be unique.";

    public static final String ARGUMENT_NOT_VALID = "Input not valid";

    // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    private ValidationError() {
    }
}
