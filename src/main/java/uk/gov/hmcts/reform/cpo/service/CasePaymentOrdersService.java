package uk.gov.hmcts.reform.cpo.service;

import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;

import java.util.List;

public interface CasePaymentOrdersService {
    List<CasePaymentOrder> getCasePaymentOrders(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter);
}
