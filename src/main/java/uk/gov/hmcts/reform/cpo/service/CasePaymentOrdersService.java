package uk.gov.hmcts.reform.cpo.service;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;

import java.util.List;
import java.util.UUID;

public interface CasePaymentOrdersService {

    Page<CasePaymentOrderEntity> getCasePaymentOrders(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter);

    CasePaymentOrder updateCasePaymentOrder(UpdateCasePaymentOrderRequest request);

    void deleteCasePaymentOrdersByIds(List<UUID> ids) throws CasePaymentOrderCouldNotBeFoundException;

    void deleteCasePaymentOrdersByCaseIds(List<Long> caseIds) throws CasePaymentOrderCouldNotBeFoundException;
}
