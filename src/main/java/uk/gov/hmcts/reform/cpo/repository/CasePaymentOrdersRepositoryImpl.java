package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.util.List;
import java.util.UUID;

@Repository
public class CasePaymentOrdersRepositoryImpl implements CasePaymentOrdersRepository {
    private final CasePaymentOrdersJpaRepository casePaymentOrdersJpaRepository;

    private final CasePaymentOrdersAuditJpaRepository casePaymentOrdersAuditJpaRepository;

    @Autowired
    public CasePaymentOrdersRepositoryImpl(CasePaymentOrdersJpaRepository casePaymentOrdersJpaRepository,
                                       CasePaymentOrdersAuditJpaRepository casePaymentOrdersAuditJpaRepository) {
        this.casePaymentOrdersJpaRepository = casePaymentOrdersJpaRepository;
        this.casePaymentOrdersAuditJpaRepository = casePaymentOrdersAuditJpaRepository;
    }

    @Override
    public void deleteByUuids(List<UUID> uuids) {
        int deleteByIds = casePaymentOrdersJpaRepository.deleteByIdIsIn(uuids);

        if (deleteByIds != uuids.size()) {
            throw new CasePaymentOrderCouldNotBeFoundException(ValidationError.CPO_NOT_FOUND_BY_ID);
        }
    }

    @Override
    public void deleteAuditEntriesByUuids(List<UUID> uuids) {
        casePaymentOrdersAuditJpaRepository.deleteByIdIn(uuids);
    }

    @Override
    public void deleteByCaseIds(List<Long> caseIds) {

        for (Long cid : caseIds) {
            if (casePaymentOrdersJpaRepository.findAllByCaseId(cid).isEmpty()) {
                throw new CasePaymentOrderCouldNotBeFoundException(ValidationError.CPO_NOT_FOUND_BY_CASE_ID);
            }
        }

        casePaymentOrdersJpaRepository.deleteByCaseIdIsIn(caseIds);
    }

    @Override
    public void deleteAuditEntriesByCaseIds(List<Long> caseIds) {
        casePaymentOrdersAuditJpaRepository.deleteByCaseIdIn(caseIds);
    }

    @Override
    public Page<CasePaymentOrderEntity> findByIdIn(List<UUID> ids, Pageable pageable) {
        return casePaymentOrdersJpaRepository.findByIdIn(ids, pageable);
    }

    @Override
    public Page<CasePaymentOrderEntity> findByCaseIdIn(List<Long> casesId, Pageable pageable) {
        return casePaymentOrdersJpaRepository.findByCaseIdIn(casesId, pageable);
    }
}
