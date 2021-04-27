package uk.gov.hmcts.reform.cpo.auditlog;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class AuditLogFormatter {

    public static final String TAG = "CLA-CPO";

    private static final String COMMA = ",";
    private static final String COLON = ":";

    public String format(AuditEntry entry) {
        List<String> formatedPairs = Lists.newArrayList(
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

        CollectionUtils.filter(formatedPairs, PredicateUtils.notNullPredicate());

        return TAG + " " + String.join(COMMA, formatedPairs);
    }

    private String getPair(String label, String value) {
        return isNotBlank(value) ? label + COLON + value : null;
    }

    private String commaSeparatedList(List<String> list) {
        return list == null ? null : list.stream().map(String::toString).collect(Collectors.joining(COMMA));
    }

}
