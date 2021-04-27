package uk.gov.hmcts.reform.cpo.auditlog;

public enum AuditOperationType {
    CREATE_CASE_PAYMENT_ORDER("CreateCasePaymentOrder"),
    DELETE_CASE_PAYMENT_ORDER("DeleteCasePaymentOrder"),
    GET_CASE_PAYMENT_ORDER("GetCasePaymentOrder"),
    UPDATE_CASE_PAYMENT_ORDER("UpdateCasePaymentOrder");

    private final String label;

    AuditOperationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
