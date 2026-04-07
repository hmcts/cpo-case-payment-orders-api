package uk.gov.hmcts.reform.cpo.service;

import java.util.List;

public interface CaseAccessService {
    void assertUserHasAccessToCase(String caseId);

    void assertUserHasAccessToCases(List<String> caseIds);

    void assertUserHasAccessToPaymentOrderIds(List<String> paymentOrderIds);

}
