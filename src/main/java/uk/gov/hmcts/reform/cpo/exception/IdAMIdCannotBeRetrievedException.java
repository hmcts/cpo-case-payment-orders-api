package uk.gov.hmcts.reform.cpo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    code = HttpStatus.BAD_REQUEST
)
public class IdAMIdCannotBeRetrievedException extends ApiException {
    public IdAMIdCannotBeRetrievedException(String message) {
        super(message);
    }
}
