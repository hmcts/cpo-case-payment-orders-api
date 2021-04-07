package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersQueryException;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.cpo.exception.ValidationError.IDAM_ID_CANNOT_BE_FOUND;
import static uk.gov.hmcts.reform.cpo.exception.ValidationError.NON_UNIQUE_PAIRING;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    private final SecurityUtils securityUtils;

    private final CasePaymentOrderMapper mapper;

    private final CasePaymentOrdersRepository casePaymentOrdersRepository;

    public CasePaymentOrdersServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository,
                                        CasePaymentOrderMapper mapper, SecurityUtils securityUtils) {
        this.mapper = mapper;
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    @Override
    public CasePaymentOrder createCasePaymentOrder(CreateCasePaymentOrderRequest createCasePaymentOrderRequest) {
        String createdBy;
        try {
            createdBy = securityUtils.getUserInfo().getUid();
        } catch (Exception e) {
            throw new CasePaymentOrdersQueryException(IDAM_ID_CANNOT_BE_FOUND);
        }
        CasePaymentOrderEntity requestEntity = mapper.toEntity(createCasePaymentOrderRequest, createdBy);

        try {
            CasePaymentOrderEntity savedEntity = casePaymentOrdersRepository.saveAndFlush(requestEntity);
            return mapper.toDomainModel(savedEntity);
        } catch (Exception e) {
            throw new CasePaymentOrdersQueryException(NON_UNIQUE_PAIRING);
        }

    }

    @Override
    public Page<CasePaymentOrderEntity> getCasePaymentOrders(final CasePaymentOrderQueryFilter
                                                                 casePaymentOrderQueryFilter) {
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

    @Override
    public void create() {

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
}
