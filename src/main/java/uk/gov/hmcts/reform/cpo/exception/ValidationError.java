package uk.gov.hmcts.reform.cpo.exception;

public final class ValidationError {

    public static final String CASE_ID_EMPTY = "Case ID can not be empty";
    public static final String CASE_ID_INVALID_LENGTH = "Case ID has to be 16-digits long";
    public static final String CASE_ID_INVALID = "Case ID has to be a valid 16-digit Luhn number";
    public static final String IDAM_ID_CANNOT_BE_FOUND = "Idam ID cannot be retrieved";
    public static final String NON_UNIQUE_PAIRING = "Order_reference and case_id pairing is non-unique";
    public static final String EFFECTIVE_FROM_EMPTY = "Effective date/time not provided";
    public static final String ACTION_EMPTY = "Action not provided";
    public static final String RESPONSIBLE_PARTY_EMPTY = "Responsible party not provided";
    public static final String ORDER_REFERENCE_EMPTY = "Order reference not provided";
    public static final String ORDER_REFERENCE_INVALID = "Order reference has invalid form";

    private ValidationError() {
    }
}
