package uk.gov.hmcts.reform.cpo.config;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyDiagnosticRedactionTest {

    @Test
    void diagnosticFilterSurvivesLoggingReloadAndIsRemovedOnContextClose() {
        LoggerContext context = new LoggerContext();
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

}
