package uk.gov.hmcts.reform.cpo.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggerContextListener;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.cpo.logging.SafeHealthDiagnosticsFilter;

@Configuration
public class SafeHealthLoggingConfiguration {

    @Bean
    public static BeanPostProcessor safeHealthLoggingPostProcessor() {
        return new HealthLoggingPostProcessor();
    }

    static final class HealthLoggingPostProcessor implements BeanPostProcessor, DisposableBean {

        private final LoggerContext context;
        private final SafeHealthDiagnosticsFilter filter = new SafeHealthDiagnosticsFilter();
        private final LoggerContextListener listener = new LoggerContextListener() {
            @Override
            public boolean isResetResistant() {
                return true;
            }

            @Override
            public void onStart(LoggerContext loggerContext) {
                installFilter();
            }

            @Override
            public void onReset(LoggerContext loggerContext) {
                installFilter();
            }

            @Override
            public void onStop(LoggerContext loggerContext) {
                // Spring context destruction removes the listener and filter.
            }

            @Override
            public void onLevelChange(Logger logger, Level level) {
                // The filter respects each source logger's effective level.
            }
        };

        private HealthLoggingPostProcessor() {
            this((LoggerContext) LoggerFactory.getILoggerFactory());
        }

        HealthLoggingPostProcessor(LoggerContext context) {
            this.context = context;
            context.addListener(listener);
            installFilter();
        }

        private void installFilter() {
            filter.setContext(context);
            filter.start();
            if (!context.getTurboFilterList().contains(filter)) {
                context.addTurboFilter(filter);
            }
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof HealthContributor contributor) {
                register(contributor);
            }
            return bean;
        }

        private void register(HealthContributor contributor) {
            if (contributor instanceof HealthIndicator) {
                filter.register(contributor.getClass());
                filter.register(AopUtils.getTargetClass(contributor));
            } else if (contributor instanceof CompositeHealthContributor composite) {
                composite.forEach(named -> register(named.getContributor()));
            }
        }

        @Override
        public void destroy() {
            context.removeListener(listener);
            context.getTurboFilterList().remove(filter);
            filter.stop();
        }
    }
}
