package uk.gov.hmcts.reform.cpo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.cpo.errorhandling.CasePaymentIdentifierException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.io.Serializable;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler({CasePaymentOrdersQueryException.class, ConstraintViolationException.class})
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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<HttpError> handleIllegalStateException(final HttpServletRequest request,
                                                              Exception exception) {
        LOG.error("Validation exception:", exception);
        if (exception.getMessage().contains("Ambiguous handler methods mapped for '/case-payment-orders'")) {
            final HttpError<Serializable> error =
                    new HttpError<>(exception, request, HttpStatus.BAD_REQUEST)
                        .withDetails("Can't delete case payment order with both id AND case-id specified");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return getHttpErrorBadRequest(request, exception);
    }

    @ExceptionHandler(CasePaymentIdentifierException.class)
    public ResponseEntity<HttpError> handleCasePaymentIdentifierException(final HttpServletRequest request,
                                                                 final Exception exception) {
        LOG.error(exception.getMessage(), exception);
        final HttpError<Serializable> error = new HttpError<>(exception, request, HttpStatus.NOT_FOUND)
                .withDetails(exception.getCause());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<HttpError> handleMethodArgumentTypeMismatchException(final HttpServletRequest request,
                                                                 final Exception exception) {
        return getHttpErrorBadRequest(request, exception);
    }
}
