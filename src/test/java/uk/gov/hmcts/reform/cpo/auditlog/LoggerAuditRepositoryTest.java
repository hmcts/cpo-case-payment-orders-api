package uk.gov.hmcts.reform.cpo.auditlog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

class LoggerAuditRepositoryTest {

    @InjectMocks
    private LoggerAuditRepository repository;

    @Mock
    private AuditLogFormatter logFormatter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should save audit entry by using AuditLogFormatter")
    void shouldSaveAuditEntry() {

        // GIVEN
        AuditEntry auditEntry = new AuditEntry();

        // WHEN
        repository.save(auditEntry);

        // THEN
        verify(logFormatter).format(auditEntry);

    }

}
