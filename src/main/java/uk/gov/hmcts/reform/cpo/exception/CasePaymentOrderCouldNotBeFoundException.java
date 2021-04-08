package uk.gov.hmcts.reform.cpo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings({"PMD.MissingSerialVersionUID"})
@ResponseStatus(
    code = HttpStatus.NOT_FOUND
)
public class CasePaymentOrderCouldNotBeFoundException extends ApiException {

    public CasePaymentOrderCouldNotBeFoundException(String message) {
        super(message);
    }

}
