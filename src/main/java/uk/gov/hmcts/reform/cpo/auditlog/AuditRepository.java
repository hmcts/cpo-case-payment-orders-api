package uk.gov.hmcts.reform.cpo.auditlog;

public interface AuditRepository {

    void save(AuditEntry auditEntry);

}
