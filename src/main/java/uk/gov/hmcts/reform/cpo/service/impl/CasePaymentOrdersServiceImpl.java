package uk.gov.hmcts.reform.cpo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CaseIdOrderReferenceUniqueConstraintException;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersFilterException;
import uk.gov.hmcts.reform.cpo.exception.IdAMIdCannotBeRetrievedException;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity.UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT;
import static uk.gov.hmcts.reform.cpo.validators.ValidationError.CANNOT_DELETE_USING_IDS_AND_CASE_IDS;
import static uk.gov.hmcts.reform.cpo.validators.ValidationError.IDAM_ID_RETRIEVE_ERROR;

@Service
@Slf4j
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    private final SecurityUtils securityUtils;

    private final CasePaymentOrderMapper mapper;

    private final CasePaymentOrdersRepository casePaymentOrdersRepository;

    @Autowired
    public CasePaymentOrdersServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository,
                                        SecurityUtils securityUtils, CasePaymentOrderMapper mapper) {
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
        this.securityUtils = securityUtils;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public CasePaymentOrder createCasePaymentOrder(CreateCasePaymentOrderRequest createCasePaymentOrderRequest) {
        String createdBy = getUserId();

        CasePaymentOrderEntity requestEntity = mapper.toEntity(createCasePaymentOrderRequest, createdBy);

        CasePaymentOrderEntity savedEntity = saveEntity(requestEntity);

        return mapper.toDomainModel(savedEntity);
    }


    @Transactional
    @Override
    public CasePaymentOrder updateCasePaymentOrder(UpdateCasePaymentOrderRequest updateCasePaymentOrderRequest) {
        String createdBy = getUserId();

        CasePaymentOrderEntity casePaymentOrderEntity = verifyCpoExists(updateCasePaymentOrderRequest.getUUID());

        mapper.mergeIntoEntity(casePaymentOrderEntity, updateCasePaymentOrderRequest, createdBy);

        CasePaymentOrderEntity updatedEntity = saveEntity(casePaymentOrderEntity);

        return mapper.toDomainModel(updatedEntity);
    }

    @Transactional
    @Override
    public void deleteCasePaymentOrders(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        validateCasePaymentOrderQueryFilter(casePaymentOrderQueryFilter);

        if (casePaymentOrderQueryFilter.isACasesIdQuery()) {
            deleteCasePaymentOrdersByCaseIds(casePaymentOrderQueryFilter.getListOfLongCasesIds());
        } else {
            deleteCasePaymentOrdersByIds(casePaymentOrderQueryFilter.getListUUID());
        }
    }

    private void deleteCasePaymentOrdersByIds(List<UUID> ids) {
        casePaymentOrdersRepository.deleteByUuids(ids);
        casePaymentOrdersRepository.deleteAuditEntriesByUuids(ids);
    }

    private void deleteCasePaymentOrdersByCaseIds(List<Long> caseIds) {
        casePaymentOrdersRepository.deleteByCaseIds(caseIds);
        casePaymentOrdersRepository.deleteAuditEntriesByCaseIds(caseIds);
    }

    private String getUserId() {
        try {
            return securityUtils.getUserInfo().getUid();
        } catch (Exception e) {
            log.error(IDAM_ID_RETRIEVE_ERROR, e);
            throw new IdAMIdCannotBeRetrievedException(IDAM_ID_RETRIEVE_ERROR);
        }
    }

    private void validateCasePaymentOrderQueryFilter(final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        if (casePaymentOrderQueryFilter.isAnIdsAndCasesIdQuery()) {
            throw new CasePaymentOrdersFilterException(
                CANNOT_DELETE_USING_IDS_AND_CASE_IDS);
        }
    }

    private boolean isDuplicateCaseIdOrderRefPairing(DataIntegrityViolationException exception) {
        return ((ConstraintViolationException) exception.getCause()).getConstraintName()
            .equals(UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT);
    }

    @Override
    public Page<CasePaymentOrder> getCasePaymentOrders(final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {

        try {
            final Page<CasePaymentOrderEntity> casePaymentOrderEntities;
            final Pageable pageRequest = casePaymentOrderQueryFilter.getPageRequest();
            if (casePaymentOrderQueryFilter.isFindByCaseIdQuery()) {
                casePaymentOrderEntities = casePaymentOrdersRepository.findByCaseIdIn(
                    casePaymentOrderQueryFilter.getListOfLongCasesIds(), pageRequest);
            } else {
                casePaymentOrderEntities = casePaymentOrdersRepository.findByIdIn(
                    casePaymentOrderQueryFilter.getListUUID(),
                    pageRequest
                );
            }
            return getPageOfCasePaymentOrder(casePaymentOrderEntities);
        } catch (IllegalArgumentException exception) {
            throw new CasePaymentOrdersFilterException(ValidationError.CPO_PAGE_ERROR);
        }
    }

    private Page<CasePaymentOrder> getPageOfCasePaymentOrder(Page<CasePaymentOrderEntity> casePaymentOrderEntities) {

        if (casePaymentOrderEntities.isEmpty()) {
            throw new CasePaymentOrderCouldNotBeFoundException(ValidationError.CPO_NOT_FOUND);
        }
        return casePaymentOrderEntities.map(casePaymentOrderEntity ->
                                                mapper.toDomainModel(casePaymentOrderEntity)
        );
    }

    private CasePaymentOrderEntity saveEntity(CasePaymentOrderEntity entity) {
        try {
            // save and flush to force unique constraint to apply now
            return casePaymentOrdersRepository.saveAndFlush(entity);

        } catch (DataIntegrityViolationException exception) {
            if (exception.getCause() instanceof ConstraintViolationException
                && isDuplicateCaseIdOrderRefPairing(exception)) {

                throw new CaseIdOrderReferenceUniqueConstraintException(ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE);
            } else {
                throw exception;
            }
        }
    }

    private CasePaymentOrderEntity verifyCpoExists(UUID id) {
        return casePaymentOrdersRepository.findById(id)
            .orElseThrow(() -> new CasePaymentOrderCouldNotBeFoundException(ValidationError.CPO_NOT_FOUND));
    }

}

