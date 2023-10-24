package uk.gov.hmcts.reform.cpo.repository;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.RandomStringUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderAuditUtils.SELECT_DELETED_ENTITIES;
import static uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderAuditUtils.SELECT_ENTITIES_ONLY;
import static uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderAuditUtils.SELECT_REVISION_INFO_ONLY;

class CasePaymentOrdersAuditTest extends BaseTest {

    @Autowired
    private CasePaymentOrdersJpaRepository casePaymentOrdersRepository;

    @Autowired
    private CasePaymentOrderMapper casePaymentOrderMapper;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    private CasePaymentOrderEntity casePaymentOrderEntity;

    private AuditReader auditReader;

    private static final String ACTION_VALUE = "Action";
    private static final String NEW_ACTION_VALUE = "New Action";

    @BeforeEach
    void setUp() {
        CasePaymentOrderEntity casePaymentOrderEntityToBePersisted =
                casePaymentOrderMapper.toEntity(CasePaymentOrder.builder()
                .orderReference(RandomStringUtils.random(10, true, true))
                .action(ACTION_VALUE)
                .caseId(new Random().nextLong())
                .createdBy("gsullivan")
                .responsibleParty("mrresponsible")
                .build()
        );
        casePaymentOrderEntity = casePaymentOrdersRepository.save(casePaymentOrderEntityToBePersisted);
        assertNotNull(casePaymentOrderEntity);

        auditReader = AuditReaderFactory.get(entityManagerFactory.createEntityManager());
    }

    @Test
    void testAuditOfCreatingCasePaymentOrder() {
        CasePaymentOrderEntity auditedCasePaymentOrderEntity =
                (CasePaymentOrderEntity) getAuditQuery(SELECT_ENTITIES_ONLY).getSingleResult();

        assertNotNull(auditedCasePaymentOrderEntity);

        assertEquals(casePaymentOrderEntity.getId(), auditedCasePaymentOrderEntity.getId());

        assertAuditDataRevisionTypes(List.of(RevisionType.ADD));
    }

    @Test
    void testAuditOfUpdatingCasePaymentOrder() {
        casePaymentOrderEntity.setAction(NEW_ACTION_VALUE);

        CasePaymentOrderEntity updateActionEntity = casePaymentOrdersRepository.save(casePaymentOrderEntity);

        assertNotNull(updateActionEntity);

        List auditedEntries = getAuditQuery(SELECT_ENTITIES_ONLY).getResultList();

        // expect two audit entries - one for the create and one for the update
        assertEquals(2, auditedEntries.size());

        // check original value and updated value
        assertEquals(ACTION_VALUE, ((CasePaymentOrderEntity)auditedEntries.get(0)).getAction());
        assertEquals(NEW_ACTION_VALUE, ((CasePaymentOrderEntity)auditedEntries.get(1)).getAction());

        assertAuditDataRevisionTypes(List.of(RevisionType.ADD, RevisionType.MOD));
    }

    @Test
    void testAuditOfDeletingCasePaymentOrder() {
        casePaymentOrdersRepository.delete(casePaymentOrderEntity);

        List auditedEntries = getAuditQuery(SELECT_ENTITIES_ONLY).getResultList();

        assertEquals(1, auditedEntries.size());

        assertAuditDataRevisionTypes(List.of(RevisionType.ADD));
    }

    private AuditQuery getAuditQuery(boolean shouldSelectEntitiesOnly) {
        return auditReader
                .createQuery()
                .forRevisionsOfEntity(CasePaymentOrderEntity.class, shouldSelectEntitiesOnly, SELECT_DELETED_ENTITIES)
                .add(AuditEntity.id().eq(casePaymentOrderEntity.getId()));
    }

    private void assertAuditDataRevisionTypes(List<RevisionType> revisionTypes) {
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = getAuditQuery(SELECT_REVISION_INFO_ONLY).getResultList();

        // Because the second boolean argument to (forRevisionsOfEntity(CasePaymentOrderEntity.class, false, true)) is
        // false a list with three-element arrays is returned
        // - the entity instance</li>
        // - revision entity, corresponding to the revision at which the entity was modified. If no custom
        // - type of the revision (an enum instance of class {@link org.hibernate.envers.RevisionType})

        // get list of revision types
        List<Object> collect = resultList.stream().map(array -> array[2]).collect(Collectors.toList());

        assertTrue(collect.containsAll(revisionTypes));
    }

}
