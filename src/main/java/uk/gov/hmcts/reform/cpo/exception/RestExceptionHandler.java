package uk.gov.hmcts.reform.cpo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
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

    @ExceptionHandler(CasePaymentOrderCouldNotBeFoundException.class)
    public ResponseEntity<Object> handleCasePaymentOrderCouldNotBeFoundException(final HttpServletRequest request,
                                                                                 final Exception exception) {

        LOG.debug("CasePaymentOrderCouldNotBeFoundException: {}", exception.getLocalizedMessage(), exception);
        final HttpError<Serializable> error = new HttpError<Serializable>(exception, request, HttpStatus.NOT_FOUND)
            .withDetails(exception.getCause());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        String[] errors = exception.getBindingResult().getFieldErrors().stream()
           .map(DefaultMessageSourceResolvable::getDefaultMessage)
           .toArray(String[]::new);
        LOG.debug("MethodArgumentNotValidException:{}", exception.getLocalizedMessage());
        final HttpError<Serializable> error = new HttpError<Serializable>(exception, request, HttpStatus.BAD_REQUEST)
            .withDetails(errors);
        return ResponseEntity
           .status(HttpStatus.BAD_REQUEST)
           .body(error);
    }

    private ResponseEntity<HttpError> getHttpErrorBadRequest(HttpServletRequest request, Exception exception) {
        LOG.error(exception.getMessage(), exception);
        final HttpError<Serializable> error = new HttpError<Serializable>(exception, request, HttpStatus.BAD_REQUEST)
            .withDetails(exception.getCause());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }
}
