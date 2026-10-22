package uk.gov.hmcts.reform.cpo.exception;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.cpo.security.ErrorCode;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.cpo.security.SafeAuthenticationErrors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.io.Serializable;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ApiException.class})
    @ResponseBody
    public ResponseEntity<HttpError<Serializable>> handleApiException(final HttpServletRequest request,
                                                                      final Exception exception) {
        final HttpError<Serializable> error = new HttpError<>(exception, request);
        logFailure(exception, error.getStatus());
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
        final HttpError<Serializable> error = new HttpError<>(exception, request, HttpStatus.BAD_REQUEST);
        logFailure(exception, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String[] errors = exception.getBindingResult().getFieldErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .toArray(String[]::new);
        final HttpError<Serializable> error = new HttpError<>(exception, request, HttpStatus.BAD_REQUEST)
            .withMessage(ValidationError.ARGUMENT_NOT_VALID)
            .withDetails(errors);
        logFailure(exception, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<HttpError<Serializable>> handleUnexpectedException(HttpServletRequest request,
                                                                            Exception exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (exception instanceof org.springframework.security.access.AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (exception instanceof org.springframework.security.core.AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
        }
        logFailure(exception, status.value());
        ResponseEntity.BodyBuilder response = ResponseEntity.status(status);
        if (status == HttpStatus.FORBIDDEN) {
            response.header(HttpHeaders.WWW_AUTHENTICATE, SafeAuthenticationErrors.ACCESS_DENIED_CHALLENGE);
        }
        return response.body(new HttpError<>(exception, request, status));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException exception, HttpHeaders headers, HttpStatusCode status,
        WebRequest request) {
        // The superclass logs the rejected method at WARN before delegating.
        return handleExceptionInternal(exception, null, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
                                                              HttpHeaders headers, HttpStatusCode status,
                                                              WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest
            && servletRequest.getResponse() != null && servletRequest.getResponse().isCommitted()) {
            logFailure(exception, status.value());
            return null;
        }
        // Never forward Spring ProblemDetail text, rejected values or parser diagnostics.
        final HttpError<Serializable> error = new HttpError<>(exception, request,
            HttpStatus.valueOf(status.value()));
        logFailure(exception, status.value());
        return super.handleExceptionInternal(exception, error, headers, status, request);
    }

    private void logFailure(Exception exception, int status) {
        log.error("Request failed: code={} status={}", ErrorCode.fromStatus(status), status, exception);
    }

}
