package uk.gov.hmcts.reform.cpo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorisedServiceException extends RuntimeException {

    private static final long serialVersionUID = 8L;

    public UnauthorisedServiceException(final String message) {
        super(message);
    }

    public UnauthorisedServiceException(final String message, final Exception cause) {
        super(message, cause);
    }
}
