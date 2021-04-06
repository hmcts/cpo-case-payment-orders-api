package uk.gov.hmcts.reform.cpo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Mock
    private CasePaymentOrderMapper casePaymentOrderMapper;
    @Mock
    private CasePaymentOrdersRepository casePaymentOrdersRepository;

    private final List<UUID> uuidsToDelete = List.of(UUID.randomUUID(), UUID.randomUUID());

    private final List<Long> caseIdsToDelete = List.of(123L, 456L);

    @BeforeEach
    public void setUp() {

        casePaymentOrdersService = new CasePaymentOrdersServiceImpl(
            casePaymentOrderMapper,
            casePaymentOrdersRepository
        );
    }

    @Test
    public void deleteCasePaymentOrdersById() throws Exception {
        casePaymentOrdersService.deleteCasePaymentOrdersByIds(uuidsToDelete);

        Mockito.verify(casePaymentOrdersRepository).deleteByUuids(uuidArgumentCaptor.capture());

        assertEquals(uuidArgumentCaptor.getValue(), uuidsToDelete);
    }

    @Test
    public void deleteCasePaymentOrdersByCaseIds() throws Exception {
        casePaymentOrdersService.deleteCasePaymentOrdersByCaseIds(caseIdsToDelete);

        Mockito.verify(casePaymentOrdersRepository).deleteByCaseIds(caseIdsArgumentCaptor.capture());

        assertEquals(caseIdsArgumentCaptor.getValue(), caseIdsToDelete);
    }
}
