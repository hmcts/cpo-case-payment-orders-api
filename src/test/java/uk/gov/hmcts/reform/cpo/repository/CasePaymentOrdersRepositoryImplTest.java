package uk.gov.hmcts.reform.cpo.repository;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
@ExtendWith(MockitoExtension.class)
public class CasePaymentOrdersRepositoryImplTest {
    @Mock
    private CasePaymentOrdersJpaRepository casePaymentOrdersJpaRepository;

    @Mock
    private CasePaymentOrdersAuditJpaRepository casePaymentOrdersAuditJpaRepository;

    @InjectMocks
    private CasePaymentOrdersRepositoryImpl casePaymentOrdersRepository;

    @Captor
    private ArgumentCaptor<List<UUID>> casePaymentOrderUuidsCaptor;

    @Captor
    private ArgumentCaptor<List<Long>> casePaymentOrderCaseIdCaptor;

    private static final List<UUID> UUIDS = List.of(UUID.randomUUID(), UUID.randomUUID());

    private static final List<Long> CASE_IDS = List.of(RandomUtils.nextLong(), RandomUtils.nextLong());

    @Test
    void testDeleteByUuids() throws Exception {
        when(casePaymentOrdersJpaRepository.deleteByIdIsIn(anyList())).thenReturn(UUIDS.size());

        casePaymentOrdersRepository.deleteByUuids(UUIDS);

        verify(casePaymentOrdersJpaRepository).deleteByIdIsIn(casePaymentOrderUuidsCaptor.capture());
        assertTrue(casePaymentOrderUuidsCaptor.getValue().containsAll(UUIDS));
    }

    @Test
    void testDeleteAuditByUuids() {
        when(casePaymentOrdersAuditJpaRepository.deleteByIdIn(anyList())).thenReturn(UUIDS.size());

        casePaymentOrdersRepository.deleteAuditEntriesByUuids(UUIDS);

        verify(casePaymentOrdersAuditJpaRepository).deleteByIdIn(casePaymentOrderUuidsCaptor.capture());
        assertTrue(casePaymentOrderUuidsCaptor.getValue().containsAll(UUIDS));
    }

    @Test
    void testExceptionThrownIfUnknownUuidCannotBeDeleted() {
        when(casePaymentOrdersJpaRepository.deleteByIdIsIn(anyList())).thenReturn(0);
        assertThrows(CasePaymentOrderCouldNotBeFoundException.class, () -> {
            casePaymentOrdersRepository.deleteByUuids(List.of(UUID.randomUUID()));
        });
    }

    @Test
    void testDeleteByCaseIds() throws Exception {
        when(casePaymentOrdersJpaRepository.deleteByCaseIdIsIn(anyList())).thenReturn(CASE_IDS.size());
        when(casePaymentOrdersJpaRepository.findAllByCaseId(anyLong()))
                .thenReturn(Collections.singletonList(new CasePaymentOrderEntity()));

        casePaymentOrdersRepository.deleteByCaseIds(CASE_IDS);

        verify(casePaymentOrdersJpaRepository).deleteByCaseIdIsIn(casePaymentOrderCaseIdCaptor.capture());
        assertTrue(casePaymentOrderCaseIdCaptor.getValue().containsAll(CASE_IDS));
    }

    @Test
    void testDeleteAuditByCaseIds() {
        when(casePaymentOrdersAuditJpaRepository.deleteByCaseIdIn(anyList())).thenReturn(CASE_IDS.size());

        casePaymentOrdersRepository.deleteAuditEntriesByCaseIds(CASE_IDS);

        verify(casePaymentOrdersAuditJpaRepository).deleteByCaseIdIn(casePaymentOrderCaseIdCaptor.capture());
        assertTrue(casePaymentOrderCaseIdCaptor.getValue().containsAll(CASE_IDS));
    }

    @Test
    void testExceptionThrownIfUnknownCaseIdCannotBeDeleted() {
        when(casePaymentOrdersJpaRepository.findAllByCaseId(anyLong())).thenReturn(Collections.emptyList());
        assertThrows(CasePaymentOrderCouldNotBeFoundException.class,
            () -> casePaymentOrdersRepository.deleteByCaseIds(List.of(123L)));
        verify(casePaymentOrdersJpaRepository, never()).deleteByCaseIdIsIn(anyList());
    }

    @Test
    void testDeleteByCaseIdsMixOfExistingAndNonExistentCaseIds() {
        // Simulate 3 records existing with case id
        when(casePaymentOrdersJpaRepository.findAllByCaseId(anyLong()))
                .thenReturn(Collections.singletonList(new CasePaymentOrderEntity()));
        when(casePaymentOrdersJpaRepository.deleteByCaseIdIsIn(anyList())).thenReturn(3);

        List<Long> caseIdToDelete = List.of(RandomUtils.nextLong());
        casePaymentOrdersRepository.deleteByCaseIds(caseIdToDelete);

        verify(casePaymentOrdersJpaRepository).deleteByCaseIdIsIn(casePaymentOrderCaseIdCaptor.capture());
        assertTrue(casePaymentOrderCaseIdCaptor.getValue().containsAll(caseIdToDelete));
    }
}
