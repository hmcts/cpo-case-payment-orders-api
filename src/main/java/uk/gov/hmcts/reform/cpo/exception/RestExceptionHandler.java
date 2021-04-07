package uk.gov.hmcts.reform.cpo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.io.Serializable;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler({CasePaymentOrdersQueryException.class,ConstraintViolationException.class})
    @ResponseBody
    public ResponseEntity<HttpError> handleCasePaymentOrdersQueryException(final HttpServletRequest request,
                                                        final Exception exception) {
        return getHttpErrorBadRequest(request, exception);
    }

    private ResponseEntity<HttpError> getHttpErrorBadRequest(HttpServletRequest request, Exception exception) {
        LOG.error(exception.getMessage(), exception);
        final HttpError<Serializable> error = new HttpError<>(exception, request,HttpStatus.BAD_REQUEST)
            .withDetails(exception.getCause());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }
}
