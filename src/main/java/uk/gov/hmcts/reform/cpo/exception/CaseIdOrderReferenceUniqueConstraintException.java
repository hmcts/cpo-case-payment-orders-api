package uk.gov.hmcts.reform.cpo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings({"PMD.MissingSerialVersionUID"})
@ResponseStatus(
    code = HttpStatus.CONFLICT
)
public class CaseIdOrderReferenceUniqueConstraintException extends ApiException {

    public CaseIdOrderReferenceUniqueConstraintException(String message) {
        super(message);
    }

}
