package uk.gov.hmcts.reform.cpo.flyway;

public class PendingMigrationScriptException extends RuntimeException {

    private static final long serialVersionUID = -3553390794631578942L;

    public PendingMigrationScriptException(String script) {
        super("Found migration not yet applied " + script);
    }
}
