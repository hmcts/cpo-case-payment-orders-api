package uk.gov.hmcts.reform.cpo.repository;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersFilterException;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
@Getter
public class CasePaymentOrderQueryFilter {


    private List<String> cpoIds;
    private List<String> caseIds;
    private Pageable pageable;


    public boolean isFindByCaseIdQuery() {
        return !caseIds.isEmpty();
    }

    public boolean isAnIdsQuery() {
        return !cpoIds.isEmpty();
    }

    public boolean isFilterByBothIdAndCaseId() {
        return isFindByCaseIdQuery() && isAnIdsQuery();
    }

    public boolean noFilters() {
        return cpoIds.isEmpty() && caseIds.isEmpty();
    }

    public List<UUID> getListUUID() {
        return cpoIds.stream().map(UUID::fromString).collect(Collectors.toList());
    }

    public List<Long> getListOfLongCasesIds() {
        return caseIds.stream().map(Long::parseLong).collect(Collectors.toList());
    }

    public Pageable getPageRequest() {
        return PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(
                Sort.Order.asc(CasePaymentOrderEntity.CASE_ID),
                Sort.Order.asc(CasePaymentOrderEntity.ORDER_REFERENCE)
            )
        );
    }

    public void validateCasePaymentOrdersFiltering() {
        if (this.isFilterByBothIdAndCaseId()) {
            throw new CasePaymentOrdersFilterException(ValidationError.CPO_FILTER_ERROR);
        }
    }

    public boolean isAnIdsAndCasesIdQuery() {
        return isACasesIdQuery() && isAnIdsQuery();
    }


    public boolean isACasesIdQuery() {
        return !caseIds.isEmpty();
    }

}
