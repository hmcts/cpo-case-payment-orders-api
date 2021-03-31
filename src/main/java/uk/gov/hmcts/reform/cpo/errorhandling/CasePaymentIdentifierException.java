package uk.gov.hmcts.reform.cpo.errorhandling;

public class CasePaymentIdentifierException extends Exception {

    private static final long serialVersionUID = 3747645268869419675L;

    public CasePaymentIdentifierException(String message) {
        super(message);
    }
}
