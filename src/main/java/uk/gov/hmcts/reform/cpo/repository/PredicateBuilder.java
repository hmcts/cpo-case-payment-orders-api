package uk.gov.hmcts.reform.cpo.repository;

import lombok.Getter;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class PredicateBuilder {

    private final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter;
    private final List<Predicate> predicates = new ArrayList<Predicate>();
    private static final String CASE_ID = "caseId";
    private static final String IDS = "id";
    private Predicate currentPredicate;

    public PredicateBuilder(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        this.casePaymentOrderQueryFilter = casePaymentOrderQueryFilter;
    }

    public void buildPredicates(final Root<CasePaymentOrderEntity> root) {

        if (isACasesIdQuery()) {
            currentPredicate = root.get(CASE_ID).in(casePaymentOrderQueryFilter.getListOfCasesIds());
            predicates.add(currentPredicate);

        }
        if (isAnIdsQuery()) {
            final List<UUID> uuids = casePaymentOrderQueryFilter.getListOfIds().stream().map(
                id -> UUID.fromString(id)
            ).collect(Collectors.toList());
            currentPredicate = root.get(IDS).in(uuids);
            predicates.add(currentPredicate);
        }
    }

    public boolean isAnAndPredicate(){
        return (isACasesIdQuery()) && (isAnIdsQuery());
    }

    private boolean isACasesIdQuery(){
        return !casePaymentOrderQueryFilter.getListOfCasesIds().isEmpty();
    }

    private boolean isAnIdsQuery(){
        return !casePaymentOrderQueryFilter.getListOfIds().isEmpty();
    }
}
