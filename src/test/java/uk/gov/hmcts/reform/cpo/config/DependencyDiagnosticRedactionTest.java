package uk.gov.hmcts.reform.cpo.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import uk.gov.hmcts.reform.cpo.logging.SafeHealthDiagnosticsFilter;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyDiagnosticRedactionTest {

    @Test
    void diagnosticFilterSurvivesLoggingReloadAndIsRemovedOnContextClose() {
        ch.qos.logback.classic.LoggerContext context = new ch.qos.logback.classic.LoggerContext();
        var processor = new SafeHealthLoggingConfiguration.HealthLoggingPostProcessor(context);
        try {
            for (int reset = 0; reset < 2; reset++) {
                context.reset();
                assertThat(context.getTurboFilterList()).singleElement()
                    .satisfies(filter -> assertThat(filter.isStarted()).isTrue());
            }
        } finally {
            processor.destroy();
        }
        assertThat(context.getTurboFilterList()).isEmpty();
        context.reset();
        assertThat(context.getTurboFilterList()).isEmpty();
        context.stop();
    }

    @Test
    void dependencyFailuresAndSlowHealthWarningsKeepDiagnosticsAtDebug() {
        new ApplicationContextRunner().withUserConfiguration(SafeHealthLoggingConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed();
            Logger boundary = (Logger) LoggerFactory.getLogger(SafeHealthDiagnosticsFilter.class);
            Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            Level previousLevel = boundary.getLevel();
            ListAppender<ILoggingEvent> appender = new ListAppender<>();
            appender.start();
            root.addAppender(appender);
            try {
                for (String name : new String[] {"com.zaxxer.hikari.pool.HikariPool",
                    "org.hibernate.engine.jdbc.spi.SqlExceptionHelper",
                    "org.springframework.boot.actuate.health.HealthEndpointSupport"}) {
                    Logger dependency = (Logger) LoggerFactory.getLogger(name);
                    Level previousDependencyLevel = dependency.getLevel();
                    try {
                        dependency.setLevel(Level.WARN);
                        boundary.setLevel(Level.INFO);
                        appender.list.clear();
                        dependency.warn("private-case-123 at jdbc:private-db",
                            new IllegalStateException("private cause"));
                        boolean intermediate = name.equals("org.hibernate.engine.jdbc.spi.SqlExceptionHelper");
                        assertThat(appender.list).hasSize(intermediate ? 0 : 1).allSatisfy(event -> {
                            assertThat(event.getFormattedMessage()).isIn(
                                "Dependency diagnostic: code=REQUEST_FAILED", "Health diagnostic: code=REQUEST_FAILED");
                            assertThat(event.getThrowableProxy()).isNull();
                        });
                        boundary.setLevel(Level.DEBUG);
                        appender.list.clear();
                        dependency.error("private-case-123 at jdbc:private-db",
                            new IllegalStateException("private cause"));
                        assertThat(appender.list).hasSize(intermediate ? 1 : 2);
                        assertThat(appender.list).filteredOn(event -> event.getLevel() != Level.DEBUG)
                            .allSatisfy(event -> assertThat(event.getFormattedMessage()).doesNotContain("private"));
                        assertThat(appender.list).filteredOn(event -> event.getLevel() == Level.DEBUG)
                            .singleElement().satisfies(event -> {
                                assertThat(event.getFormattedMessage()).contains("private-case-123");
                                assertThat(event.getThrowableProxy().getMessage()).isEqualTo("private cause");
                            });
                        dependency.setLevel(Level.ERROR);
                        appender.list.clear();
                        dependency.warn("disabled warning");
                        assertThat(appender.list).isEmpty();
                    } finally {
                        dependency.setLevel(previousDependencyLevel);
                    }
                }
            } finally {
                boundary.setLevel(previousLevel);
                root.detachAppender(appender);
                appender.stop();
            }
        });
    }
}
