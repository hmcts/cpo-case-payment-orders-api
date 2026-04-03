package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.CaseAccessClient;
import uk.gov.hmcts.reform.cpo.service.CaseAccessService;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.util.List;
import java.util.UUID;

@Service
public class CaseAccessServiceImpl implements CaseAccessService {

    private final CasePaymentOrdersRepository casePaymentOrdersRepository;
    private final SecurityUtils securityUtils;
    private final CaseAccessClient caseAccessClient;

    public CaseAccessServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository,
                                 SecurityUtils securityUtils,
                                 CaseAccessClient caseAccessClient) {
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
        this.securityUtils = securityUtils;
        this.caseAccessClient = caseAccessClient;
    }
    @Override
    public void assertUserHasAccessToCase(String caseId) {
        caseAccessClient.assertCanAccessCase(securityUtils.getUserToken(), caseId);
    }

    @Override
    public void assertUserHasAccessToCases(List<String> caseIds) {
        caseIds.stream()
            .distinct()
            .forEach(this::assertUserHasAccessToCase);
    }

    @Override
    public void assertUserHasAccessToPaymentOrderIds(List<String> paymentOrderIds) {
        List<String> caseIds = paymentOrderIds.stream()
            .map(UUID::fromString)
            .map(this::getCaseIdForPaymentOrderId)
            .toList();

        assertUserHasAccessToCases(caseIds);
    }

    private String getCaseIdForPaymentOrderId(UUID paymentOrderId) {
        return casePaymentOrdersRepository.findById(paymentOrderId)
            .orElseThrow(() -> new CasePaymentOrderCouldNotBeFoundException(ValidationError.CPO_NOT_FOUND))
            .getCaseId()
            .toString();
    }

}
