package uk.gov.hmcts.reform.cpo.repository;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersFilterException;

import java.util.ArrayList;
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

    public PageRequest getPageRequest() {
        final List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, CasePaymentOrderEntity.CASES_ID));
        orders.add(new Sort.Order(Sort.Direction.ASC, CasePaymentOrderEntity.ORDER_REFERENCE));
        return PageRequest.of(
            this.getPageNumber(),
            this.getPageSize(),
            Sort.by(orders)
        );
    }

    public void validateCasePaymentOrderQueryFilter() {
        if (this.isAnIdsAndCasesIdQuery()) {
            throw new CasePaymentOrdersFilterException(
                "case payment orders cannot be filtered by both id and case id.");
        }
    }

}
