package uk.gov.hmcts.reform.cpo.repository;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
@Getter
public class CasePaymentOrderQueryFilter {

    public static final String CASES_TYPE_ID = "caseTypeId";
    public static final String ORDER_REFERENCE = "orderReference";

    private Integer pageSize;
    private Integer pageNumber;
    private List<String> listOfIds;
    private List<String> listOfCasesIds;


    public boolean isACasesIdQuery() {
        return !listOfCasesIds.isEmpty();
    }

    public boolean isAnIdsQuery() {
        return !listOfIds.isEmpty();
    }

    public boolean isAnIdsAndCasesIdQuery() {
        return (isACasesIdQuery()) && (isAnIdsQuery());
    }

    public boolean isItAnEmptyCriteria() {
        return listOfIds.isEmpty() && listOfCasesIds.isEmpty();
    }

    public List<UUID> getListUUID() {
        return listOfIds.stream().map(id -> UUID.fromString(id)).collect(Collectors.toList());
    }

    public List<Long> getListOfLongCasesIds() {
        return listOfCasesIds.stream().map(caseId -> Long.parseLong(caseId)).collect(Collectors.toList());
    }
}
