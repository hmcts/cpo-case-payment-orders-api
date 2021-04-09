package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CasePaymentOrdersRepository extends JpaRepository<CasePaymentOrderEntity, UUID> {

    Page<CasePaymentOrderEntity> findByIdIn(List<UUID> ids, Pageable pageable);

    Page<CasePaymentOrderEntity> findByCaseIdIn(List<Long> casesId, Pageable pageable);
}
