package uk.gov.hmcts.reform.cpo.service;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;

public interface CasePaymentOrdersService {


    CasePaymentOrder createCasePaymentOrder(CreateCasePaymentOrderRequest request);

    Page<CasePaymentOrder> getCasePaymentOrders(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter);

    CasePaymentOrder updateCasePaymentOrder(UpdateCasePaymentOrderRequest request);

    void deleteCasePaymentOrders(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter);
}
