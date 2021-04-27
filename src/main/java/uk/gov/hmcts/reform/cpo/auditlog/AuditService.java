package uk.gov.hmcts.reform.cpo.auditlog;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.auditlog.aop.AuditContext;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static uk.gov.hmcts.reform.cpo.validators.ValidationError.IDAM_ID_RETRIEVE_ERROR;

@Slf4j
@Service
public class AuditService {

    private final Clock clock;
    private final SecurityUtils securityUtils;
    private final AuditRepository auditRepository;

    public AuditService(@Lazy final SecurityUtils securityUtils,
                        final AuditRepository auditRepository) {
        this.clock = Clock.systemUTC();
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

        entry.setIdamId(getUserId());
        entry.setInvokingService(auditContext.getInvokingService());

        entry.setOperationType(auditContext.getAuditOperationType() != null
                                   ? auditContext.getAuditOperationType().getLabel() : null);
        entry.setCpoIds(combineStringAndList(auditContext.getCpoIds(), auditContext.getCpoId()));
        entry.setCaseIds(combineStringAndList(auditContext.getCaseIds(), auditContext.getCaseId()));

        auditRepository.save(entry);
    }

    public String getInvokingService(HttpServletRequest request) {
        return securityUtils.getServiceNameFromS2SToken(request.getHeader(SecurityUtils.SERVICE_AUTHORIZATION));
    }

    private List<String> combineStringAndList(List<String> list, String value) {
        if (StringUtils.isNotBlank(value)) {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(value);
        }

        return list;
    }

    private String getUserId() {
        try {
            return securityUtils.getUserInfo().getUid();
        } catch (Exception e) {
            log.error(IDAM_ID_RETRIEVE_ERROR, e);
            return null;
        }
    }

}
