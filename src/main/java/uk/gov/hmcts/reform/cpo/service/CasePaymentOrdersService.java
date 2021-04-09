package uk.gov.hmcts.reform.cpo.service;

import org.springframework.data.domain.Page;

import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;

public interface CasePaymentOrdersService {

    Page<CasePaymentOrderEntity> getCasePaymentOrders(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter);

    CasePaymentOrder updateCasePaymentOrder(UpdateCasePaymentOrderRequest request);

}
