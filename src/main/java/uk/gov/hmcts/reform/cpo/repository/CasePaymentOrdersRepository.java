package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;

import java.util.UUID;

@Repository
public interface CasePaymentOrdersRepository extends JpaRepository<CasePaymentOrderEntity, UUID> {

}
