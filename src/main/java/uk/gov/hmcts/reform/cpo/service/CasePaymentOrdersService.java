package uk.gov.hmcts.reform.cpo.service;

import uk.gov.hmcts.reform.cpo.errorhandling.CasePaymentIdentifierException;

import java.util.List;
import java.util.UUID;

public interface CasePaymentOrdersService {
    void deleteCasePaymentOrdersByIds(List<UUID> ids) throws CasePaymentIdentifierException;

    void deleteCasePaymentOrdersByCaseIds(List<Long> caseIds) throws CasePaymentIdentifierException;
}
