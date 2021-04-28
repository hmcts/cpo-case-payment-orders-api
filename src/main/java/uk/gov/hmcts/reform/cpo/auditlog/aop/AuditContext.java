package uk.gov.hmcts.reform.cpo.auditlog.aop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.cpo.auditlog.AuditOperationType;

import java.util.List;

@Builder(builderMethodName = "auditContextWith")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditContext {

    private List<String> cpoIds;
    private List<String> caseIds;
    private AuditOperationType auditOperationType;

    private int httpStatus;
    private String httpMethod;
    private String requestPath;
    private String requestId;

    private String invokingService;

}
