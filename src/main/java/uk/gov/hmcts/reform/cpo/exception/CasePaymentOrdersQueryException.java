package uk.gov.hmcts.reform.cpo.exception;

public class CasePaymentOrdersQueryException extends RuntimeException {

    private static final long serialVersionUID = -1528640941755059617L;

    public CasePaymentOrdersQueryException(String message) {
        super(message);
    }

}
