package uk.gov.hmcts.reform.cpo.security;

public enum Permission {
    CREATE("C"), READ("R"), UPDATE("U"), DELETE("D");

    private final String label;

    Permission(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
