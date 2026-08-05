package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.CaseAccessClient;
import uk.gov.hmcts.reform.cpo.service.CaseAccessService;

import java.util.List;
import java.util.Optional;
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
    public void assertUserHasAccessToExistingCases(List<String> caseIds) {
        List<String> existingCaseIds = caseIds.stream()
            .distinct()
            .map(Long::parseLong)
            .filter(casePaymentOrdersRepository::existsByCaseId)
            .map(String::valueOf)
            .toList();

        assertUserHasAccessToCases(existingCaseIds);
    }

    @Override
    public void assertUserHasAccessToPaymentOrderIds(List<String> paymentOrderIds) {
        List<String> caseIds = paymentOrderIds.stream()
            .map(UUID::fromString)
            .map(casePaymentOrdersRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(casePaymentOrderEntity -> casePaymentOrderEntity.getCaseId().toString())
            .toList();

        assertUserHasAccessToCases(caseIds);
    }
}
