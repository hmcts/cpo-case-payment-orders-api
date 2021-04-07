package uk.gov.hmcts.reform.cpo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javax.validation.ValidationException;
import java.io.Serializable;
import java.util.List;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String[] errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getDefaultMessage())
            .toArray(String[]::new);
        LOG.debug("MethodArgumentNotValidException:{}", ex.getLocalizedMessage());
        return toResponseEntity(status, null, errors);
    }

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler({CasePaymentOrdersQueryException.class,ConstraintViolationException.class})
    @ResponseBody
    public ResponseEntity<HttpError> handleCasePaymentOrdersQueryException(final HttpServletRequest request,
                                                        final Exception exception) {
        return getHttpErrorBadRequest(request, exception);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(Exception ex) {
        LOG.error("Validation exception:", ex);
        return toResponseEntity(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
    }

    private ResponseEntity<HttpError> getHttpErrorBadRequest(HttpServletRequest request, Exception exception) {
        LOG.error(exception.getMessage(), exception);
        final HttpError<Serializable> error = new HttpError<Serializable>(exception, request,HttpStatus.BAD_REQUEST)
            .withDetails(exception.getCause());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    private ResponseEntity<Object> toResponseEntity(HttpStatus status, String message, String... errors) {
        ApiError apiError = new ApiError(status, message, errors == null ? null : List.of(errors));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}
