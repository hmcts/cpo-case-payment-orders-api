package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CasePaymentOrdersRepository {
    void deleteByUuids(List<UUID> uuids);

    void deleteAuditEntriesByUuids(List<UUID> uuids);

    void deleteByCaseIds(List<Long> caseIds);

    void deleteAuditEntriesByCaseIds(List<Long> caseIds);

    Optional<CasePaymentOrderEntity> findById(UUID id);

    Page<CasePaymentOrderEntity> findByIdIn(List<UUID> ids, Pageable pageable);

    Page<CasePaymentOrderEntity> findByCaseIdIn(List<Long> casesId, Pageable pageable);

    CasePaymentOrderEntity saveAndFlush(CasePaymentOrderEntity casePaymentOrderEntity);
}
