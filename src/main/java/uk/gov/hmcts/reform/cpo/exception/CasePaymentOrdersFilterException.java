package uk.gov.hmcts.reform.cpo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    code = HttpStatus.BAD_REQUEST
)
public class CasePaymentOrdersFilterException extends ApiException {

    public CasePaymentOrdersFilterException(String message) {
        super(message);
    }
}
