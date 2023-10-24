package uk.gov.hmcts.reform.cpo.auditlog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import uk.gov.hmcts.reform.cpo.auditlog.aop.AuditContext;
import uk.gov.hmcts.reform.cpo.auditlog.aop.AuditContextHolder;
import uk.gov.hmcts.reform.cpo.config.AuditConfiguration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
public class AuditInterceptor implements AsyncHandlerInterceptor {


    private final AuditConfiguration config;
    private final AuditService auditService;

    public AuditInterceptor(AuditService auditService,
                            AuditConfiguration auditConfiguration) {
        this.auditService = auditService;
        this.config = auditConfiguration;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception ex) {

        if (config.isAuditLogEnabled() && hasAuditAnnotation(handler)) {
            if (!config.isHttpStatusIgnored(response.getStatus())) {

                var auditContext = AuditContextHolder.getAuditContext();
                auditContext = populateHttpSemantics(auditContext, request, response);

                var logAuditAnnotation = ((HandlerMethod) handler).getMethodAnnotation((LogAudit.class));
                if (logAuditAnnotation != null) {
                    auditContext.setAuditOperationType(logAuditAnnotation.operationType());
                }

                try {
                    auditService.audit(auditContext);
                } catch (Exception e) {  // Ignoring audit failures
                    log.error("Error while auditing the request data:{}", e.getMessage());
                }
            }

            AuditContextHolder.remove();
        }
    }

    private boolean hasAuditAnnotation(Object handler) {
        return handler instanceof HandlerMethod && ((HandlerMethod) handler).hasMethodAnnotation(LogAudit.class);
    }

    private AuditContext populateHttpSemantics(AuditContext auditContext,
                                               HttpServletRequest request, HttpServletResponse response) {
        AuditContext context = (auditContext != null) ? auditContext : new AuditContext();
        context.setHttpStatus(response.getStatus());
        context.setHttpMethod(request.getMethod());
        context.setRequestPath(request.getRequestURI());
        context.setRequestId(request.getHeader(AuditConfiguration.REQUEST_ID));
        context.setInvokingService(auditService.getInvokingService(request));
        return context;
    }

}
