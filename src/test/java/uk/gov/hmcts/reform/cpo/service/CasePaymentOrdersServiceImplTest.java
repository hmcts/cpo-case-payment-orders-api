package uk.gov.hmcts.reform.cpo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
@ExtendWith(MockitoExtension.class)
class CasePaymentOrdersServiceImplTest {

    @Mock
    private CasePaymentOrdersRepository casePaymentOrdersRepository;

    @InjectMocks
    private CasePaymentOrdersServiceImpl casePaymentOrdersService;

    @Captor
    ArgumentCaptor<List<UUID>> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<List<Long>> caseIdsArgumentCaptor;

    private static final List<UUID> uuidsToDelete = List.of(UUID.randomUUID(), UUID.randomUUID());

    private final List<Long> caseIdsToDelete = List.of(123L, 456L);

    private CasePaymentOrderQueryFilter uuidFilter;

    private CasePaymentOrderQueryFilter caseIdFilter;

    @BeforeEach
    void beforeEachTest() {
        uuidFilter = CasePaymentOrderQueryFilter.builder()
                .listOfIds(uuidsToDelete.stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList()))
                .listOfCasesIds(Collections.emptyList())
                .build();
        caseIdFilter = CasePaymentOrderQueryFilter.builder()
                .listOfIds(Collections.emptyList())
                .listOfCasesIds(caseIdsToDelete.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .build();
    }

    @Test
    void deleteCasePaymentOrdersById() {
        casePaymentOrdersService.deleteCasePaymentOrders(uuidFilter);

        verify(casePaymentOrdersRepository).deleteByUuids(uuidArgumentCaptor.capture());
        assertEquals(uuidArgumentCaptor.getValue(), uuidsToDelete);

        verify(casePaymentOrdersRepository).deleteAuditEntriesByUuids(uuidArgumentCaptor.capture());
        assertEquals(uuidArgumentCaptor.getValue(), uuidsToDelete);
    }

    @Test
    void deleteCasePaymentOrdersByCaseIds() {
        casePaymentOrdersService.deleteCasePaymentOrders(caseIdFilter);

        verify(casePaymentOrdersRepository).deleteByCaseIds(caseIdsArgumentCaptor.capture());
        assertEquals(caseIdsArgumentCaptor.getValue(), caseIdsToDelete);

        verify(casePaymentOrdersRepository).deleteAuditEntriesByCaseIds(caseIdsArgumentCaptor.capture());
        assertEquals(caseIdsArgumentCaptor.getValue(), caseIdsToDelete);
    }
}
