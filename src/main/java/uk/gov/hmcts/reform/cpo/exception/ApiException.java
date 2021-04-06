package uk.gov.hmcts.reform.cpo.exception;

@SuppressWarnings({"PMD.MissingSerialVersionUID"})
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

}
