package uk.gov.hmcts.reform.cpo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.cpo.exception.domain.HttpError;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<HttpError> handleApiException(final HttpServletRequest request,
                                                        final Exception exception) {
        LOG.error(exception.getMessage(), exception);
        final HttpError<Serializable> error = new HttpError<>(exception, request)
            .withDetails(exception.getCause());
        return ResponseEntity
            .status(error.getStatus())
            .body(error);
    }
}
