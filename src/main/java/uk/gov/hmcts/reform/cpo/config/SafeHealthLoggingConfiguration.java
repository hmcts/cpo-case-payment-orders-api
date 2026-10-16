package uk.gov.hmcts.reform.cpo.config;

import ch.qos.logback.classic.LoggerContext;
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

    private static final class HealthLoggingPostProcessor implements BeanPostProcessor, DisposableBean {

        private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        private final SafeHealthDiagnosticsFilter filter = new SafeHealthDiagnosticsFilter();

        private HealthLoggingPostProcessor() {
            filter.setContext(context);
            filter.start();
            context.addTurboFilter(filter);
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
            context.getTurboFilterList().remove(filter);
            filter.stop();
        }
    }
}
