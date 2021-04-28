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
    public static final String ORDER_REFERENCE_REQUIRED = "Order Reference is required.";
    public static final String ORDER_REFERENCE_INVALID = "Order Reference has invalid format.";
    public static final String RESPONSIBLE_PARTY_REQUIRED = "Responsible Party is required.";
    public static final String CPO_FILER_ERROR = "Case payment orders cannot be filtered by both id and case id.";
    public static final String CPO_PAGE_ERROR = "Case Payment Order, Page index must not zero or be less than zero!";

    public static final String CPO_NOT_FOUND_BY_ID = "Case Payment Order specified by IDs does not exist";
    public static final String CPO_NOT_FOUND_BY_CASE_ID = "Case Payment Order specified by Case IDs does not exist";
    public static final String CANNOT_DELETE_USING_IDS_AND_CASE_IDS =
            "Cannot delete Case Payment Orders by both ids and case-ids";

    // additional constraints etc..
    public static final String CPO_NOT_FOUND = "Case Payment Order does not exist.";
    public static final String CASE_ID_ORDER_REFERENCE_UNIQUE
        = "A case payment order with the specified Order Reference already exists on the case.";
    public static final String IDAM_ID_RETRIEVE_ERROR = "Idam ID cannot be retrieved.";

    public static final String ARGUMENT_NOT_VALID = "Input not valid";
    public static final String INVALID_PERMISSION_WHITELIST_VALUE = "Valid values for permission are: CRUD";

    // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    private ValidationError() {
    }

}
