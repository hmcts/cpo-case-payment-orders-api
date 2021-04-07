package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderAuditEntity;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface CasePaymentOrdersAuditJpaRepository extends JpaRepository<CasePaymentOrderAuditEntity, UUID> {
    @Modifying
    @Query("DELETE FROM CasePaymentOrderAuditEntity cpoae where cpoae.caseId in :caseIds")
    int deleteByCaseIdIn(@Param("caseIds") List<Long> caseIds);

    @Modifying
    @Query("DELETE FROM CasePaymentOrderAuditEntity cpoae where cpoae.id in :ids")
    int deleteByIdIn(@Param("ids") List<UUID> ids);

}
