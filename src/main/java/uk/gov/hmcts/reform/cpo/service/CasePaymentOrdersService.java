package uk.gov.hmcts.reform.cpo.service;

import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;

public interface CasePaymentOrdersService {
    CasePaymentOrder createCasePaymentOrder(CreateCasePaymentOrderRequest request, String userToken);
}
