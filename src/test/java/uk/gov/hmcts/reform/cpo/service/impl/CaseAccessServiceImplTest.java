package uk.gov.hmcts.reform.cpo.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.CaseAccessClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class CaseAccessServiceImplTest {

    @Test
    @DisplayName("should resolve payment order ids to case ids before checking access")
    void shouldResolvePaymentOrderIdsToCaseIdsBeforeCheckingAccess() {
        CasePaymentOrdersRepository casePaymentOrdersRepository = mock(CasePaymentOrdersRepository.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);
        CaseAccessClient caseAccessClient = mock(CaseAccessClient.class);

        final CaseAccessServiceImpl service = new CaseAccessServiceImpl(
            casePaymentOrdersRepository,
            securityUtils,
            caseAccessClient
        );

        final UUID paymentOrderId1 = UUID.randomUUID();
        final UUID paymentOrderId2 = UUID.randomUUID();

        CasePaymentOrderEntity entity1 = new CasePaymentOrderEntity();
        entity1.setCaseId(1234567890123456L);

        CasePaymentOrderEntity entity2 = new CasePaymentOrderEntity();
        entity2.setCaseId(2234567890123456L);

        when(casePaymentOrdersRepository.findById(paymentOrderId1)).thenReturn(Optional.of(entity1));
        when(casePaymentOrdersRepository.findById(paymentOrderId2)).thenReturn(Optional.of(entity2));
        when(securityUtils.getUserToken()).thenReturn("Bearer user-token");

        service.assertUserHasAccessToPaymentOrderIds(List.of(
            paymentOrderId1.toString(),
            paymentOrderId2.toString()
        ));

        verify(caseAccessClient).assertCanAccessCase("Bearer user-token", "1234567890123456");
        verify(caseAccessClient).assertCanAccessCase("Bearer user-token", "2234567890123456");
    }


    @Test
    @DisplayName("should ignore missing payment order ids and only check access for found payment orders")
    void shouldIgnoreMissingPaymentOrderIdsAndOnlyCheckAccessForFoundPaymentOrders() {
        CasePaymentOrdersRepository repository = mock(CasePaymentOrdersRepository.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);
        CaseAccessClient caseAccessClient = mock(CaseAccessClient.class);

        final CaseAccessServiceImpl service = new CaseAccessServiceImpl(repository, securityUtils, caseAccessClient);

        UUID foundPaymentOrderId = UUID.randomUUID();
        UUID missingPaymentOrderId = UUID.randomUUID();

        CasePaymentOrderEntity entity = new CasePaymentOrderEntity();
        entity.setCaseId(1234567890123456L);

        when(repository.findById(foundPaymentOrderId)).thenReturn(Optional.of(entity));
        when(repository.findById(missingPaymentOrderId)).thenReturn(Optional.empty());
        when(securityUtils.getUserToken()).thenReturn("Bearer user-token");

        service.assertUserHasAccessToPaymentOrderIds(List.of(
            foundPaymentOrderId.toString(),
            missingPaymentOrderId.toString()
        ));

        verify(caseAccessClient).assertCanAccessCase("Bearer user-token", "1234567890123456");
        verifyNoMoreInteractions(caseAccessClient);
    }


    @Test
    @DisplayName("should use current user token when checking access to a case")
    void shouldUseCurrentUserTokenWhenCheckingAccessToCase() {
        CasePaymentOrdersRepository casePaymentOrdersRepository = mock(CasePaymentOrdersRepository.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);
        CaseAccessClient caseAccessClient = mock(CaseAccessClient.class);

        when(securityUtils.getUserToken()).thenReturn("Bearer user-token");

        CaseAccessServiceImpl service = new CaseAccessServiceImpl(
            casePaymentOrdersRepository,
            securityUtils,
            caseAccessClient
        );

        service.assertUserHasAccessToCase("1234567890123456");

        verify(caseAccessClient).assertCanAccessCase("Bearer user-token", "1234567890123456");
    }

    @Test
    @DisplayName("should check each distinct case id")
    void shouldCheckEachDistinctCaseId() {
        CasePaymentOrdersRepository casePaymentOrdersRepository = mock(CasePaymentOrdersRepository.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);
        CaseAccessClient caseAccessClient = mock(CaseAccessClient.class);

        when(securityUtils.getUserToken()).thenReturn("Bearer user-token");

        CaseAccessServiceImpl service = new CaseAccessServiceImpl(
            casePaymentOrdersRepository,
            securityUtils,
            caseAccessClient
        );

        service.assertUserHasAccessToCases(List.of(
            "1234567890123456",
            "1234567890123456",
            "2234567890123456"
        ));

        verify(caseAccessClient).assertCanAccessCase("Bearer user-token", "1234567890123456");
        verify(caseAccessClient).assertCanAccessCase("Bearer user-token", "2234567890123456");
        verifyNoMoreInteractions(caseAccessClient);
    }
}
