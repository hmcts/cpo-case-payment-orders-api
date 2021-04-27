package uk.gov.hmcts.reform.cpo.auditlog.aop;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cpo.auditlog.LogAudit;

import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@ConditionalOnProperty(name = "audit.log.enabled", havingValue = "true")
@Slf4j
public class AuditAspect {

    private static final String RESULT_VARIABLE = "result";

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Around(value = "@annotation(logAudit)")
    public Object audit(ProceedingJoinPoint joinPoint, LogAudit logAudit) throws Throwable {
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            String cpoId =  getValue(joinPoint, logAudit.cpoId(), result, String.class);
            List<String> cpoIds =  getValueAsList(joinPoint, logAudit.cpoIds(), result);
            String caseId =  getValue(joinPoint, logAudit.caseId(), result, String.class);
            List<String> caseIds =  getValueAsList(joinPoint, logAudit.caseIds(), result);

            AuditContextHolder.setAuditContext(AuditContext.auditContextWith()
                                                   .auditOperationType(logAudit.operationType())
                                                   .cpoId(cpoId)
                                                   .cpoIds(cpoIds)
                                                   .caseId(caseId)
                                                   .caseIds(caseIds)
                                                   .build());
        }
    }

    private <T> T getValue(JoinPoint joinPoint, String condition, Object result, Class<T> returnType) {
        if (StringUtils.isNotBlank(condition) && !(result == null && condition.contains(RESULT_VARIABLE))) {
            var method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            try {
                var evaluationContext = evaluator.createEvaluationContext(joinPoint.getThis(),
                                                                                        joinPoint.getThis().getClass(),
                                                                                        method,
                                                                                        joinPoint.getArgs());
                evaluationContext.setVariable(RESULT_VARIABLE, result);
                var methodKey = new AnnotatedElementKey(method, joinPoint.getThis().getClass());
                return evaluator.condition(condition, methodKey, evaluationContext, returnType);
            } catch (SpelEvaluationException ex) {
                log.warn("Error evaluating LogAudit annotation expression:{} on method:{}",
                         condition, method.getName(), ex);
                return null;
            }
        }
        return null;
    }

    private List<String> getValueAsList(JoinPoint joinPoint, String condition, Object result) {
        List<?> response = getValue(joinPoint, condition, result, List.class);

        return response != null ? response.stream().map(Object::toString).collect(Collectors.toList()) : null;
    }

}

