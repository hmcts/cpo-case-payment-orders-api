package uk.gov.hmcts.reform.cpo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.io.Serializable;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ApiException.class})
    @ResponseBody
    public ResponseEntity<HttpError<Serializable>> handleApiException(final HttpServletRequest request,
                                                                      final Exception exception) {
        log.error(exception.getMessage(), exception);
        final HttpError<Serializable> error = new HttpError<>(exception, request);
        return ResponseEntity
            .status(error.getStatus())
            .body(error);
    }

    @ExceptionHandler({
        ConstraintViolationException.class,
        MethodArgumentTypeMismatchException.class
    })
    @ResponseBody
    public ResponseEntity<HttpError<Serializable>> handleCommonExceptionsAsBadRequest(final HttpServletRequest request,
                                                                                      final Exception exception) {
        log.error(exception.getMessage(), exception);
        final HttpError<Serializable> error = new HttpError<>(exception, request, HttpStatus.BAD_REQUEST)
            .withDetails(exception.getCause());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
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
        log.debug("MethodArgumentNotValidException:{}", exception.getLocalizedMessage());
        final HttpError<Serializable> error = new HttpError<>(exception, request, HttpStatus.BAD_REQUEST)
            .withMessage(ValidationError.ARGUMENT_NOT_VALID)
            .withDetails(errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

}
