package uk.gov.hmcts.reform.cpo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.cpo.auditlog.AuditInterceptor;
import uk.gov.hmcts.reform.cpo.auditlog.AuditService;

import java.util.List;

@EnableAspectJAutoProxy
@Configuration
public class AuditConfiguration implements WebMvcConfigurer {

    public static final String REQUEST_ID = "request-id";

    private final AuditService auditService;

    @Getter
    private final boolean auditLogEnabled;

    private final List<Integer> auditLogIgnoreStatuses;

    @Autowired
    public AuditConfiguration(final AuditService auditService,
                              @Value("${audit.log.enabled:true}")
                                  boolean auditLogEnabled,
                              @Value("#{'${audit.log.ignore.statuses}'.split(',')}")
                                      List<Integer> auditLogIgnoreStatuses) {
        super();
        this.auditService = auditService;
        this.auditLogEnabled = auditLogEnabled;
        this.auditLogIgnoreStatuses = auditLogIgnoreStatuses;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createAuditInterceptor());
    }

    private AuditInterceptor createAuditInterceptor() {
        return new AuditInterceptor(auditService, this);
    }

    public boolean isHttpStatusIgnored(int status) {
        return auditLogIgnoreStatuses.contains(status);
    }

}
