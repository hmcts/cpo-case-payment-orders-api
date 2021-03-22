package uk.gov.hmcts.reform.cpo.service;

import uk.gov.hmcts.reform.cpo.payload.CasePaymentOrderRequest;

import java.util.UUID;

public interface CasePaymentOrdersService {
    UUID createCasePaymentOrder(CasePaymentOrderRequest request);

    boolean checkParametersPresent(CasePaymentOrderRequest request);

}
