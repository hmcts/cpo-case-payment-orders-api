package uk.gov.hmcts.reform.cpo.service;

import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;

public interface CasePaymentOrdersService {
    CasePaymentOrder createCasePaymentOrder(CreateCasePaymentOrderRequest request);
    Page<CasePaymentOrderEntity> getCasePaymentOrders(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter);
}
