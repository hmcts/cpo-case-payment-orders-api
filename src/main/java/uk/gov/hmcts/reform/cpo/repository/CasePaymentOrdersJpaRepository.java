package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CasePaymentOrdersJpaRepository extends JpaRepository<CasePaymentOrderEntity, UUID> {
    int deleteByIdIsIn(Collection<UUID> id);

    int deleteByCaseIdIsIn(Collection<Long> caseIds);

    int countAllById(UUID uuid);

    int countAllByCaseId(Long caseId);

    Page<CasePaymentOrderEntity> findByIdIn(List<UUID> ids, Pageable pageable);

    Page<CasePaymentOrderEntity> findByCaseIdIn(List<Long> casesId, Pageable pageable);
}
