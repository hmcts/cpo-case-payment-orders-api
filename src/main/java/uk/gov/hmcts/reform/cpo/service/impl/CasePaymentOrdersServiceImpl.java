package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersQueryException;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {


    private final CasePaymentOrdersRepository casePaymentOrdersRepository;

    @Autowired
    public CasePaymentOrdersServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository) {
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
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
        orders.add(new Sort.Order(Sort.Direction.ASC,  CasePaymentOrderQueryFilter.CASES_TYPE_ID));
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

    //TODO THIS IS NOT GOING TO BE ADDED IN THE FINAL PR
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void create() {

        final CasePaymentOrderEntity casePaymentOrderEntity = new CasePaymentOrderEntity();
        casePaymentOrderEntity.setAction("action");
        casePaymentOrderEntity.setCaseId(Long.parseLong("1609243447569251"));
        casePaymentOrderEntity.setCreatedBy("action1");
        casePaymentOrderEntity.setOrderReference("action1");
        casePaymentOrderEntity.setEffectiveFrom(LocalDateTime.now());
        casePaymentOrderEntity.setCreatedTimestamp(LocalDateTime.now());
        casePaymentOrderEntity.setResponsibleParty("setResponsibleParty");
        casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity);
        casePaymentOrdersRepository.flush();

        final CasePaymentOrderEntity casePaymentOrderEntity1 = new CasePaymentOrderEntity();
        casePaymentOrderEntity1.setAction("action");
        casePaymentOrderEntity1.setCaseId(Long.parseLong("1609243447569252"));
        casePaymentOrderEntity1.setCreatedBy("action1");
        casePaymentOrderEntity1.setOrderReference("Baction2");
        casePaymentOrderEntity1.setEffectiveFrom(LocalDateTime.now());
        casePaymentOrderEntity1.setCreatedTimestamp(LocalDateTime.now());
        casePaymentOrderEntity1.setResponsibleParty("setResponsibleParty");
        casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity1);
        casePaymentOrdersRepository.flush();

        final CasePaymentOrderEntity casePaymentOrderEntity2 = new CasePaymentOrderEntity();
        casePaymentOrderEntity2.setAction("action");
        casePaymentOrderEntity2.setCaseId(Long.parseLong("1609243447569253"));
        casePaymentOrderEntity2.setCreatedBy("action1");
        casePaymentOrderEntity2.setOrderReference("Caction3");
        casePaymentOrderEntity2.setEffectiveFrom(LocalDateTime.now());
        casePaymentOrderEntity2.setCreatedTimestamp(LocalDateTime.now());
        casePaymentOrderEntity2.setResponsibleParty("setResponsibleParty");
        casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity2);
        casePaymentOrdersRepository.flush();

    }
}
