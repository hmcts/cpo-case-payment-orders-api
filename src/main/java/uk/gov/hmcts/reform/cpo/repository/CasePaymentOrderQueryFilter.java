package uk.gov.hmcts.reform.cpo.repository;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
@Getter
public class CasePaymentOrderQueryFilter {

    public static final Integer PAGE_NUMBER = 0;

    private Integer pageSize;
    private Integer pageNumber;
    private List<String> cpoIds;
    private List<String> caseIds;


    public boolean isACasesIdQuery() {
        return !caseIds.isEmpty();
    }

    public boolean isAnIdsQuery() {
        return !cpoIds.isEmpty();
    }

    public boolean isAnIdsAndCasesIdQuery() {
        return isACasesIdQuery() && isAnIdsQuery();
    }

    public boolean isItAnEmptyCriteria() {
        return cpoIds.isEmpty() && caseIds.isEmpty();
    }

    public List<UUID> getListUUID() {
        return cpoIds.stream().map(UUID::fromString).collect(Collectors.toList());
    }

    public List<Long> getListOfLongCasesIds() {
        return caseIds.stream().map(Long::parseLong).collect(Collectors.toList());
    }
}
