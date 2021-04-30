package uk.gov.hmcts.reform.cpo.auditlog;

import lombok.Data;

import java.util.List;

@Data
public class AuditEntry {

    private String dateTime;

    private String operationType;

    private String idamId;
    private String invokingService;

    private int httpStatus;
    private String httpMethod;
    private String requestPath;
    private String requestId;

    private List<String> cpoIds;
    private List<String> caseIds;

}
