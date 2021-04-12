package uk.gov.hmcts.reform.cpo.repository;

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

    List<CasePaymentOrderEntity> findAllByCaseId(Long caseId);
}
