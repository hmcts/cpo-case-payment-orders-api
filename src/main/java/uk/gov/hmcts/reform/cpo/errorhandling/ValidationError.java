package uk.gov.hmcts.reform.cpo.errorhandling;

public final class ValidationError {

    public static final String IDS_EMPTY = "Ids can not be empty";
    public static final String CASE_ID_INVALID_LENGTH = "Case ID has to be 16-digits long";
    public static final String CASE_IDS_EMPTY = "Case ID can not be empty";
    public static final String CASE_IDS_INVALID = "Case ID has to be a valid 16-digit Luhn number";

    // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    private ValidationError() {
    }
}
