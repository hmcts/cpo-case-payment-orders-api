package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;

import java.util.List;
import org.springframework.data.domain.Pageable;

@Repository
public interface CasePaymentOrdersRepository extends JpaRepository<CasePaymentOrderEntity, Long> {

    @Query("select b from CasePaymentOrdersRepository b where b.id in :ids")
    Slice<CasePaymentOrderEntity> getCasePaymentOrdersByIds(@Param("ids") List<String> ids, Pageable pageable);


    @Query("select b from CasePaymentOrdersRepository b where b.caseId in :casesId")
    Slice<CasePaymentOrderEntity> getCasePaymentOrdersByCaseIds(@Param("casesId") List<String> casesId, Pageable pageable);

}
