package uk.gov.hmcts.reform.cpo.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.cpo.errorhandling.CasePaymentIdentifierException;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl.AUDIT_ENTRY_DELETION_ERROR;

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

    private final List<UUID> uuidsToDelete = List.of(UUID.randomUUID(), UUID.randomUUID());

    private final List<Long> caseIdsToDelete = List.of(123L, 456L);

    private static final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @BeforeAll
    static void setUp() {
        createAndStartTestLogger();
    }

    @BeforeEach
    void beforeEachTest() {
        listAppender.list.clear();
    }

    private static void createAndStartTestLogger() {
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CasePaymentOrdersServiceImpl.class);
        listAppender.start();

        logger.addAppender(listAppender);
    }

    @Test
    void deleteCasePaymentOrdersById() throws Exception {
        casePaymentOrdersService.deleteCasePaymentOrdersByIds(uuidsToDelete);

        verify(casePaymentOrdersRepository).deleteByUuids(uuidArgumentCaptor.capture());
        assertEquals(uuidArgumentCaptor.getValue(), uuidsToDelete);

        verify(casePaymentOrdersRepository).deleteAuditEntriesByUuids(uuidArgumentCaptor.capture());
        assertEquals(uuidArgumentCaptor.getValue(), uuidsToDelete);
    }

    @Test
    void deleteCasePaymentOrdersById_UserNotifiedIfAuditIsNotDeleted() throws CasePaymentIdentifierException {
        doThrow(IllegalStateException.class).when(casePaymentOrdersRepository).deleteAuditEntriesByUuids(anyList());


        casePaymentOrdersService.deleteCasePaymentOrdersByIds(uuidsToDelete);

        List<ILoggingEvent> logsList = listAppender.list;

        assertEquals(1, logsList.size());
        assertEquals(AUDIT_ENTRY_DELETION_ERROR.replace("{}", uuidsToDelete.toString()),
                logsList.get(0).getFormattedMessage());
    }

    @Test
    void deleteCasePaymentOrdersByCaseId_UserNotifiedIfAuditIsNotDeleted() throws CasePaymentIdentifierException {
        doThrow(IllegalStateException.class).when(casePaymentOrdersRepository).deleteAuditEntriesByCaseIds(anyList());

        casePaymentOrdersService.deleteCasePaymentOrdersByCaseIds(caseIdsToDelete);

        List<ILoggingEvent> logsList = listAppender.list;

        assertEquals(1, logsList.size());
        assertEquals(AUDIT_ENTRY_DELETION_ERROR.replace("{}", caseIdsToDelete.toString()),
                logsList.get(0).getFormattedMessage());
    }

    @Test
    void deleteCasePaymentOrdersByCaseIds() throws Exception {
        casePaymentOrdersService.deleteCasePaymentOrdersByCaseIds(caseIdsToDelete);

        verify(casePaymentOrdersRepository).deleteByCaseIds(caseIdsArgumentCaptor.capture());
        assertEquals(caseIdsArgumentCaptor.getValue(), caseIdsToDelete);

        verify(casePaymentOrdersRepository).deleteAuditEntriesByCaseIds(caseIdsArgumentCaptor.capture());
        assertEquals(caseIdsArgumentCaptor.getValue(), caseIdsToDelete);
    }
}
