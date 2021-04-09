package uk.gov.hmcts.reform.cpo.exception;

public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

}
