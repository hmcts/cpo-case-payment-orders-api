package uk.gov.hmcts.reform.cpo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.FormattingTuple;
import uk.gov.hmcts.reform.cpo.security.ErrorCode;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps health and dependency diagnostics behind an owned DEBUG logger. */
@Slf4j
public class SafeHealthDiagnosticsFilter extends TurboFilter {

    private final Set<String> indicatorLoggers = ConcurrentHashMap.newKeySet();

    public void register(Class<?> indicatorClass) {
        for (Class<?> type = indicatorClass; type != null && type != Object.class; type = type.getSuperclass()) {
            indicatorLoggers.add(type.getName());
        }
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format,
                              Object[] arguments, Throwable exception) {
        // Null format denotes a level check, not an emitted log event.
        if (format == null || !level.isGreaterOrEqual(Level.WARN)
            || !level.isGreaterOrEqual(logger.getEffectiveLevel())) {
            return FilterReply.NEUTRAL;
        }
        boolean health = indicatorLoggers.contains(logger.getName())
            || logger.getName().startsWith("org.springframework.boot.actuate.health.");
        if (!health && logger.getName().startsWith("uk.gov.hmcts.reform.cpo.")) {
            return FilterReply.NEUTRAL;
        }
        String name = logger.getName();
        boolean boundaryLogsFailure = name.equals("org.hibernate.engine.jdbc.spi.SqlExceptionHelper")
            || name.startsWith("org.hibernate.orm.jdbc.batch")
            || name.startsWith("org.springframework.transaction.interceptor.")
            || name.equals("org.springframework.transaction.support.TransactionTemplate")
            || name.equals("org.springframework.web.servlet.PageNotFound")
            || name.startsWith("org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver")
            || name.equals("org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver");
        // Intermediate JDBC/transaction/MVC diagnostics precede the terminal failure summary.
        if (!boundaryLogsFailure) {
            String summary = health ? "Health diagnostic" : "Dependency diagnostic";
            if (level == Level.ERROR) {
                log.error("{}: code={}", summary, ErrorCode.INTERNAL_ERROR);
            } else {
                log.warn("{}: code={}", summary, ErrorCode.REQUEST_FAILED);
            }
        }
        FormattingTuple diagnostic = MessageFormatter.arrayFormat(format, arguments);
        log.debug("Original diagnostic: component={} message={}", logger.getName(),
            diagnostic.getMessage(), exception != null ? exception : diagnostic.getThrowable());
        return FilterReply.DENY;
    }
}
