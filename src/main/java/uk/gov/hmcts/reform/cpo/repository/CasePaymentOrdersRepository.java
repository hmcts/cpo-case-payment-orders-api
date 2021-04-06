package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.errorhandling.CasePaymentIdentifierException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;

import java.util.List;
import java.util.UUID;

@Transactional(rollbackFor = CasePaymentIdentifierException.class)
public interface CasePaymentOrdersRepository {
    void deleteByUuids(List<UUID> uuids) throws CasePaymentIdentifierException;

    void deleteAuditEntriesByUuids(List<UUID> uuids);

    void deleteByCaseIds(List<Long> caseIds) throws CasePaymentIdentifierException;

    void deleteAuditEntriesByCaseIds(List<Long> caseIds);

    Page<CasePaymentOrderEntity> findByIdIn(List<UUID> ids, Pageable pageable);

    Page<CasePaymentOrderEntity> findByCaseIdIn(List<Long> casesId, Pageable pageable);
}
