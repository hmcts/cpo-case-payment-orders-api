package uk.gov.hmcts.reform.cpo.service.impl;

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
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersQueryException;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    protected static final String UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT = "unique_case_id_order_reference";

    @Autowired
    private final CasePaymentOrderMapper casePaymentOrderMapper;
    @Autowired
    private final CasePaymentOrdersRepository casePaymentOrdersRepository;
    @Autowired
    private final SecurityUtils securityUtils;

    public CasePaymentOrdersServiceImpl(CasePaymentOrderMapper casePaymentOrderMapper,
                                        CasePaymentOrdersRepository casePaymentOrdersRepository,
                                        SecurityUtils securityUtils) {
        this.casePaymentOrderMapper = casePaymentOrderMapper;
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
        this.securityUtils = securityUtils;
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
                "case-payment-orders cannot filter case payments orders by both id and cases-id.");
        }
    }

    @Transactional
    @Override
    public CasePaymentOrder updateCasePaymentOrder(UpdateCasePaymentOrderRequest updateCasePaymentOrderRequest) {
        String createdBy = securityUtils.getUserInfo().getUid();

        // verify CPO exists upfront
        CasePaymentOrderEntity casePaymentOrderEntity =
            casePaymentOrdersRepository.findById(UUID.fromString(updateCasePaymentOrderRequest.getId()))
                .orElseThrow(() -> new CasePaymentOrderCouldNotBeFoundException(ValidationError.CPO_NOT_FOUND));

        casePaymentOrderMapper.mergeIntoEntity(casePaymentOrderEntity, updateCasePaymentOrderRequest, createdBy);

        CasePaymentOrderEntity returnEntity;

        try {
            // save and flush to force unique constraint to apply now
            returnEntity = casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity);

        } catch (DataIntegrityViolationException exception) {
            if (exception.getCause() instanceof ConstraintViolationException
                && isDuplicateCaseIdOrderRefPairing(exception)) {

                throw new CaseIdOrderReferenceUniqueConstraintException(ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE);
            } else {
                throw exception;
            }
        }

        return casePaymentOrderMapper.toDomainModel(returnEntity);
    }

    private boolean isDuplicateCaseIdOrderRefPairing(DataIntegrityViolationException exception) {
        return ((ConstraintViolationException) exception.getCause()).getConstraintName()
            .equals(UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT);
    }
}
