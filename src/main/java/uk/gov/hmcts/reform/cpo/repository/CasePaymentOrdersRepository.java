package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;

import java.util.List;
import java.util.UUID;

@Transactional(rollbackFor = CasePaymentOrderCouldNotBeFoundException.class)
public interface CasePaymentOrdersRepository {
    void deleteByUuids(List<UUID> uuids);

    void deleteAuditEntriesByUuids(List<UUID> uuids);

    void deleteByCaseIds(List<Long> caseIds);

    void deleteAuditEntriesByCaseIds(List<Long> caseIds);

    Page<CasePaymentOrderEntity> findByIdIn(List<UUID> ids, Pageable pageable);

    Page<CasePaymentOrderEntity> findByCaseIdIn(List<Long> casesId, Pageable pageable);

    CasePaymentOrderEntity saveAndFlush(CasePaymentOrderEntity casePaymentOrderEntity);
}
