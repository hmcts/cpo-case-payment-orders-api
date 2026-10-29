package uk.gov.hmcts.reform.cpo.exception;

import lombok.Getter;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.cpo.security.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Getter
public class HttpError<T extends Serializable> implements Serializable {

    public static final Integer DEFAULT_STATUS = HttpStatus.INTERNAL_SERVER_ERROR.value();
    public static final String DEFAULT_ERROR = "Unexpected Error";

    private final transient LocalDateTime timestamp;
    private final Integer status;
    private final String error;
    private final ErrorCode code;
    private String message;
    private T details;

    public HttpError(Exception exception, String path, HttpStatus status) {
        final var responseStatus = exception.getClass().getAnnotation(ResponseStatus.class);

        this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
        this.status = getStatusFromResponseStatus(responseStatus, status);
        this.error = getErrorReason(responseStatus, status);
        this.code = ErrorCode.fromStatus(this.status);
        this.message = this.error;
        if (exception instanceof CaseIdOrderReferenceUniqueConstraintException) {
            this.message = ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE;
        } else if (exception instanceof CasePaymentOrdersFilterException
            && (ValidationError.CPO_FILTER_ERROR.equals(exception.getMessage())
                || ValidationError.CANNOT_DELETE_USING_IDS_AND_CASE_IDS.equals(exception.getMessage())
                || ValidationError.CPO_PAGE_ERROR.equals(exception.getMessage()))) {
            this.message = exception.getMessage();
        }
    }

    public HttpError(Exception exception, HttpServletRequest request, HttpStatus status) {
        this(exception, request.getRequestURI(), status);
    }

    public HttpError(Exception exception, WebRequest request, HttpStatus status) {
        this(exception, ((ServletWebRequest)request).getRequest().getRequestURI(), status);
    }

    public HttpError(Exception exception, HttpServletRequest request) {
        this(exception, request, null);
    }

    private Integer getStatusFromResponseStatus(ResponseStatus responseStatus, HttpStatus status) {
        if (status != null) {
            return status.value();
        }
        if (null != responseStatus) {
            final var httpStatus = getHttpStatus(responseStatus);
            if (null != httpStatus) {
                return httpStatus.value();
            }
        }

        return DEFAULT_STATUS;
    }

    private HttpStatus getHttpStatus(ResponseStatus responseStatus) {
        if (!HttpStatus.INTERNAL_SERVER_ERROR.equals(responseStatus.value())) {
            return responseStatus.value();
        } else if (!HttpStatus.INTERNAL_SERVER_ERROR.equals(responseStatus.code())) {
            return responseStatus.code();
        }

        return null;
    }

    private String getErrorReason(ResponseStatus responseStatus, HttpStatus status) {
        if (null != responseStatus) {
            final var httpStatus = getHttpStatus(responseStatus);
            if (null != httpStatus) {
                return httpStatus.getReasonPhrase();
            }
        } else if (null != status) {
            return status.getReasonPhrase();
        }

        return DEFAULT_ERROR;
    }

    public HttpError<T> withDetails(T details) {
        this.details = details;
        return this;
    }

    public HttpError<T> withMessage(String message) {
        this.message = message;
        return this;
    }

}
