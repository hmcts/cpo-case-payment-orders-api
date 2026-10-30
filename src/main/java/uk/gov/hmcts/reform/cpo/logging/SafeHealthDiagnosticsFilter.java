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

/** Retains health and dependency diagnostics in administrator-controlled server logs. */
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
        // Preserve the native event if the replacement logger would suppress it.
        if (level == Level.ERROR ? !log.isErrorEnabled() : !log.isWarnEnabled()) {
            return FilterReply.NEUTRAL;
        }
        FormattingTuple diagnostic = MessageFormatter.arrayFormat(format, arguments);
        Throwable cause = exception != null ? exception : diagnostic.getThrowable();
        if (level == Level.ERROR) {
            log.error("Dependency diagnostic: code={} logger={} message={}",
                ErrorCode.INTERNAL_ERROR, logger.getName(), diagnostic.getMessage(), cause);
        } else {
            log.warn("Dependency diagnostic: code={} logger={} message={}",
                ErrorCode.REQUEST_FAILED, logger.getName(), diagnostic.getMessage(), cause);
        }
        return FilterReply.DENY;
    }
}
