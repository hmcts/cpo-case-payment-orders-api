package uk.gov.hmcts.reform.cpo.auditlog;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cpo.config.AuditConfiguration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Repository
public class AuditLogFormatter {

    private final AuditConfiguration config;

    public static final String TAG = "CLA-CPO";

    private static final String COMMA = ",";
    private static final String COLON = ":";

    @Autowired
    public AuditLogFormatter(@Lazy AuditConfiguration config) {
        this.config = config;
    }

    public String format(AuditEntry entry) {
        List<String> formattedPairs = Lists.newArrayList(
            getPair("dateTime", entry.getDateTime()),
            getPair("operationType", entry.getOperationType()),
            getPair("idamId", entry.getIdamId()),
            getPair("invokingService", entry.getInvokingService()),
            getPair("endpointCalled", entry.getHttpMethod() + " " + entry.getRequestPath()),
            getPair("operationalOutcome", String.valueOf(entry.getHttpStatus())),
            getPair("cpoId", commaSeparatedList(entry.getCpoIds())),
            getPair("caseId", commaSeparatedList(entry.getCaseIds())),
            getPair("X-Request-ID", entry.getRequestId())
        );

        CollectionUtils.filter(formattedPairs, PredicateUtils.notNullPredicate());

        return TAG + " " + String.join(COMMA, formattedPairs);
    }

    private String getPair(String label, String value) {
        return isNotBlank(value) ? label + COLON + value : null;
    }

    private String commaSeparatedList(List<String> list) {
        if (list == null) {
            return null;
        }

        Stream<String> stream = list.stream();
        if (config.getAuditLogMaxListSize() > 0) {
            stream = stream.limit(config.getAuditLogMaxListSize());
        }

        return stream.collect(Collectors.joining(COMMA));
    }

}
