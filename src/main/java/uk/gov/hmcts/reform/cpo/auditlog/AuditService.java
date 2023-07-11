package uk.gov.hmcts.reform.cpo.auditlog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.auditlog.aop.AuditContext;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

@Slf4j
@Service
public class AuditService {

    private final Clock clock;
    private final SecurityUtils securityUtils;
    private final AuditRepository auditRepository;

    public AuditService(@Qualifier("utcClock") final Clock clock,
                        @Lazy final SecurityUtils securityUtils,
                        final AuditRepository auditRepository) {
        this.clock = clock;
        this.securityUtils = securityUtils;
        this.auditRepository = auditRepository;
    }

    public void audit(AuditContext auditContext) {
        var entry = new AuditEntry();

        String formattedDate = LocalDateTime.now(clock).format(ISO_LOCAL_DATE_TIME);
        entry.setDateTime(formattedDate);

        entry.setHttpStatus(auditContext.getHttpStatus());
        entry.setHttpMethod(auditContext.getHttpMethod());
        entry.setRequestPath(auditContext.getRequestPath());
        entry.setRequestId(auditContext.getRequestId());

        entry.setIdamId(securityUtils.getUserInfo().getUid());
        entry.setInvokingService(auditContext.getInvokingService());

        entry.setOperationType(auditContext.getAuditOperationType() != null
                                   ? auditContext.getAuditOperationType().getLabel() : null);
        entry.setCpoIds(auditContext.getCpoIds());
        entry.setCaseIds(auditContext.getCaseIds());

        auditRepository.save(entry);
    }

    public String getInvokingService(HttpServletRequest request) {
        return securityUtils.getServiceNameFromS2SToken(request.getHeader(SecurityUtils.SERVICE_AUTHORIZATION));
    }

}
