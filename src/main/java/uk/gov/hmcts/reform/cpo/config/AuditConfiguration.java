package uk.gov.hmcts.reform.cpo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.cpo.auditlog.AuditInterceptor;
import uk.gov.hmcts.reform.cpo.auditlog.AuditService;
import uk.gov.hmcts.reform.cpo.auditlog.aop.AuditAspect;

import java.util.List;

@EnableAspectJAutoProxy
@Configuration
@ComponentScan
public class AuditConfiguration implements WebMvcConfigurer {

    public static final String REQUEST_ID = "request-id";

    private final AuditService auditService;

    @Getter
    private final boolean auditLogEnabled;

    private final List<Integer> auditLogIgnoreStatuses;

    @Getter
    private final int auditLogMaxListSize;

    @Autowired
    public AuditConfiguration(final AuditService auditService,
                              @Value("${audit.log.enabled:true}")
                                  boolean auditLogEnabled,
                              @Value("#{'${audit.log.ignore.statuses}'.split(',')}")
                                      List<Integer> auditLogIgnoreStatuses,
                              @Value("${audit.log.max-list-size:0}")
                                      int auditLogMaxListSize) {
        super();
        this.auditService = auditService;
        this.auditLogEnabled = auditLogEnabled;
        this.auditLogIgnoreStatuses = auditLogIgnoreStatuses;
        this.auditLogMaxListSize = auditLogMaxListSize;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createAuditInterceptor());
    }

    @Bean
    public AuditInterceptor auditInterceptor() {
        return createAuditInterceptor();
    }

    @Bean
    public AuditAspect auditAspect() {
        return new AuditAspect();
    }

    private AuditInterceptor createAuditInterceptor() {
        return new AuditInterceptor(auditService, this);
    }

    public boolean isHttpStatusIgnored(int status) {
        return auditLogIgnoreStatuses.contains(status);
    }

}
