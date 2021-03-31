package uk.gov.hmcts.reform.cpo.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderAuditEntity;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CasePaymentOrdersAuditRepositoryTest extends BaseTest {

    @Autowired
    CasePaymentOrdersAuditJpaRepository casePaymentOrdersAuditRepository;

    @Autowired
    CasePaymentOrdersJpaRepository casePaymentOrdersRepository;

    @Autowired
    private CasePaymentOrderEntityGenerator casePaymentOrderEntityGenerator;

    @Autowired
    private RevInfoRepository revInfoRepository;

    @BeforeEach
    public void setUp() {
        casePaymentOrdersRepository.deleteAllInBatch();
        casePaymentOrdersAuditRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("delete from case-payment-orders-audit table using UUIDs")
    class DeleteCasePaymentOrdersByUUIDs {

        /**
         * Delete a single ID - Audit entry has one row for that ID.
         */
        @DisplayName("test deletion of an audit entity that has a single corresponding row from audit table")
        @Test
        public void testDeleteEntitySingleIdMatches() {
            // Given
            List<CasePaymentOrderEntity> persistedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1);
            assertEquals(persistedEntities.size(), casePaymentOrdersAuditRepository.findAll().size());

            // When
            casePaymentOrdersAuditRepository.deleteByIdIn(List.of(persistedEntities.get(0).getId()));

            // Then
            assertTrue(casePaymentOrdersAuditRepository.findAll().isEmpty());
            assertTrue(revInfoRepository.findAll().isEmpty());
        }

        /**
         * Deleting multiple IDs - Audit entry has one row for each ID.
         */
        @DisplayName("test deletion of multiple audit entities that each have a single row, from audit table")
        @Test
        public void testDeleteEntitiesSingleIdMatches() {
            // Given
            List<CasePaymentOrderEntity> persistedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(3);
            assertEquals(persistedEntities.size(), casePaymentOrdersAuditRepository.findAll().size());

            // When
            casePaymentOrdersAuditRepository.deleteByIdIn(List.of(persistedEntities.get(0).getId(),
                                                                    persistedEntities.get(1).getId()));

            // Then
            assertEquals(persistedEntities.size() - 2, casePaymentOrdersAuditRepository.findAll().size());
            assertEquals(1, revInfoRepository.findAll().size());
        }

        /**
         * Dleting a single ID - Audit entry has multiple rows for that ID.
         */
        @DisplayName("test deletion of single audit entity that has multiple rows, from audit table")
        @Test
        public void testDeleteEntityMultipleIdMatches() {
            // Given
            List<CasePaymentOrderEntity> persistedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1);
            assertEquals(persistedEntities.size(), casePaymentOrdersAuditRepository.findAll().size());

            updateCasePaymentOrderAuditEntries(List.of(persistedEntities.get(0)));

            // audit table should now have 2 entries - one for the creation of each entity and the 2 updates
            assertEquals(persistedEntities.size() + 1, casePaymentOrdersAuditRepository.findAll().size());

            // When
            casePaymentOrdersAuditRepository.deleteByIdIn(List.of(persistedEntities.get(0).getId()));

            // Then
            assertTrue(casePaymentOrdersAuditRepository.findAll().isEmpty());
            assertTrue(revInfoRepository.findAll().isEmpty());
        }

        /**
         * Deleting multiple IDs - Audit entry has multiple rows for each ID.
         */
        @DisplayName("test deletion of multiple audit entities that each have multiple rows, from audit table")
        @Test
        public void testDeleteEntitiesMultipleIdMatches() {
            // Given
            List<CasePaymentOrderEntity> persistedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(3);
            assertEquals(persistedEntities.size(), casePaymentOrdersAuditRepository.findAll().size());

            updateCasePaymentOrderAuditEntries(List.of(persistedEntities.get(0), persistedEntities.get(1)));

            // audit table should now have 5 entries - one for the creation of each entity and the 2 updates
            assertEquals(persistedEntities.size() + 2, casePaymentOrdersAuditRepository.findAll().size());

            // When
            casePaymentOrdersAuditRepository.deleteByIdIn(List.of(persistedEntities.get(0).getId(),
                    persistedEntities.get(1).getId()));

            // Then
            assertEquals(1, casePaymentOrdersAuditRepository.findAll().size());
            assertEquals(1, revInfoRepository.findAll().size());
        }

        private List<CasePaymentOrderAuditEntity> updateCasePaymentOrderAuditEntries(
                                                                List<CasePaymentOrderEntity> casePaymentOrderEntities) {
            return casePaymentOrderEntities.stream()
                    .map(casePaymentOrderEntity -> {
                        casePaymentOrderEntity.setAction("new Action");
                        return casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity);
                    })
                    .map(casePaymentOrderEntity -> new CasePaymentOrderAuditEntity(casePaymentOrderEntity.getId()))
                    .collect(Collectors.toList());
        }
    }

    @Nested
    @DisplayName("delete from case-payment-orders-audit table using CaseIds")
    class DeleteCasePaymentOrdersByCaseIds {

        /**
         * Deleting a single Case ID - Audit entry has one row for that ID.
         */
        @DisplayName("test deletion of an audit entity that has a single corresponding row from audit table")
        @Test
        public void testDeleteEntitySingleCaseIdMatches() {
            // Given
            List<CasePaymentOrderEntity> persistedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1);
            assertEquals(persistedEntities.size(), casePaymentOrdersAuditRepository.findAll().size());

            CasePaymentOrderAuditEntity entityToDelete = new CasePaymentOrderAuditEntity();
            entityToDelete.setCaseId(persistedEntities.get(0).getCaseId());

            // When
            casePaymentOrdersAuditRepository.deleteByCaseIdIn(List.of(persistedEntities.get(0).getCaseId()));

            // Then
            assertTrue(casePaymentOrdersAuditRepository.findAll().isEmpty());
            assertTrue(revInfoRepository.findAll().isEmpty());
        }

        /**
         * Deleting multiple IDs - Audit entry has one row for each ID.
         */
        @DisplayName("test deletion of multiple audit entities that each have a single row, from audit table")
        @Test
        public void testDeleteEntitiesSingleCaseIdMatches() {
            // Given
            List<CasePaymentOrderEntity> persistedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(3);
            assertEquals(persistedEntities.size(), casePaymentOrdersAuditRepository.findAll().size());

            CasePaymentOrderAuditEntity entityToDelete = new CasePaymentOrderAuditEntity();
            entityToDelete.setCaseId(persistedEntities.get(0).getCaseId());

            CasePaymentOrderAuditEntity entityToDelete2 = new CasePaymentOrderAuditEntity();
            entityToDelete2.setCaseId(persistedEntities.get(1).getCaseId());

            // When
            casePaymentOrdersAuditRepository.deleteByCaseIdIn(List.of(persistedEntities.get(0).getCaseId(),
                                                                        persistedEntities.get(1).getCaseId()));

            // Then
            assertEquals(persistedEntities.size() - 2, casePaymentOrdersAuditRepository.findAll().size());
            assertEquals(1, revInfoRepository.findAll().size());
        }

        /**
         * Deleting a single ID - Audit entry has multiple rows for that ID.
         */
        @DisplayName("test deletion of single audit entity that has multiple audiot entries rows, from audit table")
        @Test
        public void testDeleteEntityMultipleIdMatches() {
            // Given
            List<CasePaymentOrderEntity> persistedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1);
            assertEquals(persistedEntities.size(), casePaymentOrdersAuditRepository.findAll().size());

            updateCasePaymentOrderAuditEntries(List.of(persistedEntities.get(0)));

            // audit table should now have 2 entries - one for the creation of each entity and the 2 updates
            assertEquals(persistedEntities.size() + 1, casePaymentOrdersAuditRepository.findAll().size());

            // When
            casePaymentOrdersAuditRepository.deleteByCaseIdIn(List.of(persistedEntities.get(0).getCaseId()));

            // Then
            assertTrue(casePaymentOrdersAuditRepository.findAll().isEmpty());
            assertTrue(revInfoRepository.findAll().isEmpty());
        }

        /**
         *  We are deleting multiple IDs - Audit entry has multiple rows for each  ID.
         */
        @DisplayName("test deletion of multiple audit entities that each have multiple rows, from audit table")
        @Test
        public void testDeleteEntitiesMultipleIdMatches() {
            // Given
            List<CasePaymentOrderEntity> persistedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(3);
            assertEquals(persistedEntities.size(), casePaymentOrdersAuditRepository.findAll().size());

            updateCasePaymentOrderAuditEntries(List.of(persistedEntities.get(0), persistedEntities.get(1)));

            // audit table should now have 5 entries - one for the creation of each entity and the 2 updates
            assertEquals(persistedEntities.size() + 2, casePaymentOrdersAuditRepository.findAll().size());

            // When
            casePaymentOrdersAuditRepository.deleteByCaseIdIn(List.of(persistedEntities.get(0).getCaseId(),
                                                                        persistedEntities.get(1).getCaseId()));

            // Then
            assertEquals(1, casePaymentOrdersAuditRepository.findAll().size());
            assertEquals(1, revInfoRepository.findAll().size());
        }

        private List<CasePaymentOrderAuditEntity> updateCasePaymentOrderAuditEntries(
            List<CasePaymentOrderEntity> casePaymentOrderEntities) {
            return casePaymentOrderEntities.stream()
                    .map(casePaymentOrderEntity -> {
                        casePaymentOrderEntity.setAction("new Action");
                        return casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity);
                    })
                    .map(casePaymentOrderEntity -> {
                        CasePaymentOrderAuditEntity cpoEntity =
                                new CasePaymentOrderAuditEntity(casePaymentOrderEntity.getId());
                        cpoEntity.setCaseId(casePaymentOrderEntity.getCaseId());
                        return cpoEntity;
                    })
                    .collect(Collectors.toList());
        }
    }
}
