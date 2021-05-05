package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        validateAllEntriesExistByUuid(uuids);
        casePaymentOrdersJpaRepository.deleteByIdIsIn(uuids);
    }

    @Override
    public void deleteAuditEntriesByUuids(List<UUID> uuids) {
        casePaymentOrdersAuditJpaRepository.deleteByIdIn(uuids);
    }

    @Override
    public void deleteByCaseIds(List<Long> caseIds) {
        validateAllEntriesExistByCaseIds(caseIds);
        casePaymentOrdersJpaRepository.deleteByCaseIdIsIn(caseIds);
    }

    private void validateAllEntriesExistByCaseIds(List<Long> caseIds) {
        List<String> nonExistentCaseIds = new ArrayList<>();
        for (Long cid : caseIds) {
            if (casePaymentOrdersJpaRepository.findAllByCaseId(cid).isEmpty()) {
                nonExistentCaseIds.add(String.valueOf(cid));
            }
        }
        throwExceptionIfCposNotFound(nonExistentCaseIds);
    }

    private void validateAllEntriesExistByUuid(List<UUID> uuids) {
        List<String> nonExistentUuids = new ArrayList<>();
        for (UUID uuid : uuids) {
            if (casePaymentOrdersJpaRepository.findAllById(uuid).isEmpty()) {
                nonExistentUuids.add(uuid.toString());
            }
        }
        throwExceptionIfCposNotFound(nonExistentUuids);
    }

    private void throwExceptionIfCposNotFound(List<String> nonExistentCpoIdentitifers) {
        if (!nonExistentCpoIdentitifers.isEmpty()) {
            throw new CasePaymentOrderCouldNotBeFoundException(
                    ValidationError.CPOS_NOT_FOUND + String.join(",", nonExistentCpoIdentitifers));
        }
    }

    @Override
    public void deleteAuditEntriesByCaseIds(List<Long> caseIds) {
        casePaymentOrdersAuditJpaRepository.deleteByCaseIdIn(caseIds);
    }

    @Override
    public Optional<CasePaymentOrderEntity> findById(UUID id) {
        return casePaymentOrdersJpaRepository.findById(id);
    }

    @Override
    public Page<CasePaymentOrderEntity> findByIdIn(List<UUID> ids, Pageable pageable) {
        validateAllEntriesExistByUuid(ids);
        return casePaymentOrdersJpaRepository.findByIdIn(ids, pageable);
    }

    @Override
    public Page<CasePaymentOrderEntity> findByCaseIdIn(List<Long> caseIds, Pageable pageable) {
        validateAllEntriesExistByCaseIds(caseIds);
        return casePaymentOrdersJpaRepository.findByCaseIdIn(caseIds, pageable);
    }

    @Override
    public CasePaymentOrderEntity saveAndFlush(CasePaymentOrderEntity casePaymentOrderEntity) {
        return casePaymentOrdersJpaRepository.saveAndFlush(casePaymentOrderEntity);
    }

}
