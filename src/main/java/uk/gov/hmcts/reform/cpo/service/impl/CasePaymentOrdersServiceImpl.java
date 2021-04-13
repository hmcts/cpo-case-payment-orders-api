package uk.gov.hmcts.reform.cpo.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CaseIdOrderReferenceUniqueConstraintException;
import uk.gov.hmcts.reform.cpo.exception.IdAMIdCannotBeRetrievedException;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.exception.CaseIdOrderReferenceUniqueConstraintException;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersQueryException;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.cpo.validators.ValidationError.CANNOT_DELETE_USING_IDS_AND_CASE_IDS;
import java.util.UUID;

import static uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity.UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT;

import static uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity.UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT;
import static uk.gov.hmcts.reform.cpo.validators.ValidationError.IDAM_ID_NOT_FOUND;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    private static final Logger LOG = LoggerFactory.getLogger(CasePaymentOrdersServiceImpl.class);

    public static final String AUDIT_ENTRY_DELETION_ERROR = "Exception thrown when deleting audit entry for case "
                                                            + "payment orders '{}'. Unwanted previous versions of the"
                                                            + " case payment orders may remain";

    @Autowired
    private final CasePaymentOrderMapper casePaymentOrderMapper;
    @Autowired
    private final SecurityUtils securityUtils;

    private final CasePaymentOrderMapper mapper;

    private final CasePaymentOrdersRepository casePaymentOrdersRepository;
    @Autowired
    private final SecurityUtils securityUtils;

    public CasePaymentOrdersServiceImpl(CasePaymentOrderMapper casePaymentOrderMapper,
                                        CasePaymentOrdersRepository casePaymentOrdersRepository,
                                        SecurityUtils securityUtils) {
        this.casePaymentOrderMapper = casePaymentOrderMapper;
    public CasePaymentOrdersServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository,
                                        SecurityUtils securityUtils, CasePaymentOrderMapper mapper) {
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
        this.securityUtils = securityUtils;
        this.securityUtils = securityUtils;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public CasePaymentOrder createCasePaymentOrder(CreateCasePaymentOrderRequest createCasePaymentOrderRequest) {
        String createdBy;
        try {
            createdBy = securityUtils.getUserInfo().getUid();
        } catch (Exception e) {
            throw new IdAMIdCannotBeRetrievedException(IDAM_ID_NOT_FOUND);
        }
        CasePaymentOrderEntity requestEntity = mapper.toEntity(createCasePaymentOrderRequest, createdBy);

        try {
            CasePaymentOrderEntity savedEntity = casePaymentOrdersRepository.saveAndFlush(requestEntity);
            return mapper.toDomainModel(savedEntity);
        } catch (DataIntegrityViolationException exception) {
            if (exception.getCause() instanceof ConstraintViolationException
                && isDuplicateCaseIdOrderRefPairing(exception)) {
                throw new CaseIdOrderReferenceUniqueConstraintException(ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE);
            } else {
                throw exception;
            }
        }
    }


    @Override
    public Page<CasePaymentOrderEntity> getCasePaymentOrders(
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {

        if (casePaymentOrderQueryFilter.isItAnEmptyCriteria()) {
            return Page.empty();
        }
        validateCasePaymentOrderQueryFilter(casePaymentOrderQueryFilter);

        final PageRequest pageRequest = getPageRequest(casePaymentOrderQueryFilter);
        if (casePaymentOrderQueryFilter.isACasesIdQuery()) {
            return casePaymentOrdersRepository.findByCaseIdIn(
                casePaymentOrderQueryFilter.getListOfLongCasesIds(),
                pageRequest
            );
        } else {
            return casePaymentOrdersRepository.findByIdIn(
                casePaymentOrderQueryFilter.getListUUID(),
                pageRequest
            );
        }
    }

    @Transactional
    @Override
    public CasePaymentOrder updateCasePaymentOrder(UpdateCasePaymentOrderRequest updateCasePaymentOrderRequest) {
        String createdBy = securityUtils.getUserInfo().getUid();

        CasePaymentOrderEntity casePaymentOrderEntity = verifyCpoExists(updateCasePaymentOrderRequest.getUUID());

        casePaymentOrderMapper.mergeIntoEntity(casePaymentOrderEntity, updateCasePaymentOrderRequest, createdBy);

        CasePaymentOrderEntity updatedEntity;

        try {
            // save and flush to force unique constraint to apply now
            updatedEntity = casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity);

        } catch (DataIntegrityViolationException exception) {
            if (exception.getCause() instanceof ConstraintViolationException
                && isDuplicateCaseIdOrderRefPairing(exception)) {

                throw new CaseIdOrderReferenceUniqueConstraintException(ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE);
            } else {
                throw exception;
            }
        }

        return casePaymentOrderMapper.toDomainModel(updatedEntity);
    }

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
        try {
            casePaymentOrdersRepository.deleteAuditEntriesByUuids(ids);
        } catch (Exception e) {
            LOG.error(AUDIT_ENTRY_DELETION_ERROR, ids);
        }
    }

    private void deleteCasePaymentOrdersByCaseIds(List<Long> caseIds) {
        casePaymentOrdersRepository.deleteByCaseIds(caseIds);
        try {
            casePaymentOrdersRepository.deleteAuditEntriesByCaseIds(caseIds);
        } catch (Exception e) {
            LOG.error(AUDIT_ENTRY_DELETION_ERROR, caseIds);
        }
    }

    private PageRequest getPageRequest(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        final List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, CasePaymentOrderQueryFilter.CASES_ID));
        orders.add(new Sort.Order(Sort.Direction.ASC, CasePaymentOrderQueryFilter.ORDER_REFERENCE));
        return PageRequest.of(
            casePaymentOrderQueryFilter.getPageNumber(),
            casePaymentOrderQueryFilter.getPageSize(),
            Sort.by(orders)
        );
    }

    private void validateCasePaymentOrderQueryFilter(final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        if (casePaymentOrderQueryFilter.isAnIdsAndCasesIdQuery()) {
            throw new CasePaymentOrdersQueryException(
                    CANNOT_DELETE_USING_IDS_AND_CASE_IDS);
        }
    }

    private boolean isDuplicateCaseIdOrderRefPairing(DataIntegrityViolationException exception) {
        return ((ConstraintViolationException) exception.getCause()).getConstraintName()
            .equals(UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT);
    }

    private boolean isDuplicateCaseIdOrderRefPairing(DataIntegrityViolationException exception) {
        return ((ConstraintViolationException) exception.getCause()).getConstraintName()
            .equals(UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT);
    }

    private CasePaymentOrderEntity verifyCpoExists(UUID id) {
        return casePaymentOrdersRepository.findById(id)
            .orElseThrow(() -> new CasePaymentOrderCouldNotBeFoundException(ValidationError.CPO_NOT_FOUND));
    }

}
