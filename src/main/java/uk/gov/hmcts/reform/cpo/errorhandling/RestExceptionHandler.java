package uk.gov.hmcts.reform.cpo.errorhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(Exception ex) {
        log.error("Validation exception:", ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(Exception ex) {
        log.error("Validation exception:", ex);
        if (ex.getMessage().contains("Ambiguous handler methods mapped for '/case-payment-orders'")) {
            return ResponseEntity
                    .badRequest()
                    .body("Can't delete case payment order with both id AND case-id specified");
        }
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(CasePaymentIdentifierException.class)
    public ResponseEntity<Object> handleUuidNotFoundException(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

}
