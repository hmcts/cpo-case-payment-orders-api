package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class CasePaymentOrdersQueryBuilder {

    @PersistenceContext
    private final EntityManager entityManager;

    public CasePaymentOrdersQueryBuilder(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<CasePaymentOrderEntity> findCasePaymentOrderByCriteria(final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {

        final PredicateBuilder predicateBuilder = new PredicateBuilder(casePaymentOrderQueryFilter);
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<CasePaymentOrderEntity> criteriaQuery = criteriaBuilder.createQuery(CasePaymentOrderEntity.class);
        final Root<CasePaymentOrderEntity> root = criteriaQuery.from(CasePaymentOrderEntity.class);

        predicateBuilder.buildPredicates(root);

        if (predicateBuilder.isAnAndPredicate()) {
            criteriaQuery.where(criteriaBuilder.and(predicateBuilder.getPredicates().toArray(new Predicate[predicateBuilder.getPredicates().size()])));
        } else {
            criteriaQuery.where(predicateBuilder.getCurrentPredicate());
        }

        final TypedQuery<CasePaymentOrderEntity> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult(casePaymentOrderQueryFilter.getPageNumber());
        query.setMaxResults(casePaymentOrderQueryFilter.getPageSize());
        return query.getResultList();
    }
}
