package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.errorhandling.CasePaymentIdentifierException;

import java.util.List;
import java.util.UUID;

@Transactional(rollbackFor = CasePaymentIdentifierException.class)
public interface CasePaymentOrdersRepository {
    void deleteByUuids(List<UUID> uuids) throws CasePaymentIdentifierException;

    void deleteAuditEntriesByUuids(List<UUID> uuids);

    void deleteByCaseIds(List<Long> caseIds) throws CasePaymentIdentifierException;

    void deleteAuditEntriesByCaseIds(List<Long> caseIds);
}
