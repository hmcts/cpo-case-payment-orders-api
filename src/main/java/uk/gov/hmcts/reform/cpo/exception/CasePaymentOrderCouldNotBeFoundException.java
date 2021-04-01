package uk.gov.hmcts.reform.cpo.exception;

@SuppressWarnings({"PMD.MissingSerialVersionUID"})
public class CasePaymentOrderCouldNotBeFoundException extends RuntimeException {

    public CasePaymentOrderCouldNotBeFoundException(String message) {
        super(message);
    }

}
