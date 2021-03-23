package uk.gov.hmcts.reform.cpo.repository;

import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
public final class PostgresqlContainer extends PostgreSQLContainer<PostgresqlContainer> {
    private static final String IMAGE_VERSION = "postgres:11";
    private static PostgresqlContainer container;

    private PostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    public static synchronized PostgresqlContainer getInstance() {
        if (container == null) {
            container = new PostgresqlContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("CPO_CASE_PAYMENT_ORDERS_DB_HOST", container.getHost());
        System.setProperty("CPO_CASE_PAYMENT_ORDERS_DB_NAME", container.getDatabaseName());
        System.setProperty("CPO_CASE_PAYMENT_ORDERS_DB_USER", container.getUsername());
        System.setProperty("CPO_CASE_PAYMENT_ORDERS_DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
