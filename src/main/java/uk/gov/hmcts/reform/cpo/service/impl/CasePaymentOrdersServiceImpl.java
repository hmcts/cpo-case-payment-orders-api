package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.errorhandling.CasePaymentIdentifierException;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;

import java.util.List;
import java.util.UUID;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    private final CasePaymentOrdersRepository casePaymentOrdersRepository;

    @Autowired
    public CasePaymentOrdersServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository) {
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
    }

    @Override
    public void deleteCasePaymentOrdersByIds(List<UUID> ids) throws CasePaymentIdentifierException {
        casePaymentOrdersRepository.deleteByUuids(ids);
        casePaymentOrdersRepository.deleteAuditEntriesByUuids(ids);
    }

    @Override
    public void deleteCasePaymentOrdersByCaseIds(List<Long> caseIds) throws CasePaymentIdentifierException {
        casePaymentOrdersRepository.deleteByCaseIds(caseIds);
        casePaymentOrdersRepository.deleteAuditEntriesByCaseIds(caseIds);
    }
}
