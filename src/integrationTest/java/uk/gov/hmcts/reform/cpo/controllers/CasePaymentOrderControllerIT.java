package uk.gov.hmcts.reform.cpo.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersAuditJpaRepository;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersJpaRepository;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;
import uk.gov.hmcts.reform.cpo.utils.UIDService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS;

public class CasePaymentOrderControllerIT extends BaseTest {

    @Autowired
    private CasePaymentOrdersJpaRepository casePaymentOrdersJpaRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CasePaymentOrdersAuditJpaRepository casePaymentOrdersAuditJpaRepository;

    @Autowired
    private UIDService uidService;

    @Autowired
    private CasePaymentOrderEntityGenerator casePaymentOrderEntityGenerator;

    @BeforeEach
    @Transactional
    public void setUp() {
        casePaymentOrdersJpaRepository.deleteAllInBatch();
        casePaymentOrdersAuditJpaRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders?ids=")
    class DeleteCasePaymentOrdersByIds {

        private static final String IDS = "ids";

        @DisplayName("Successfully delete single case payment order specified by an id")
        @Test
        void shouldDeleteSingleCasePaymentSpecifiedById() throws Exception {

            CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS).queryParam(IDS, savedEntity.getId().toString()))
                    .andExpect(status().isOk());

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }

        @DisplayName("Successfully delete multiple case payments with specified ids")
        @Test
        void shouldDeleteMultipleCasePaymentsSpecifiedByIds() throws Exception {

            List<CasePaymentOrderEntity> savedEntities
                    = casePaymentOrderEntityGenerator.generateAndSaveEntities(3);
            List<UUID> savedEntitiesUuids = savedEntities.stream()
                    .map(CasePaymentOrderEntity::getId)
                    .collect(Collectors.toList());
            assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());

            String[] savedEntitiesUuidsString = savedEntitiesUuids.stream().map(UUID::toString).toArray(String[]::new);

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS).queryParam(IDS, savedEntitiesUuidsString))
                    .andExpect(status().isOk());

            assertTrue(casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
            assertTrue(casePaymentOrdersAuditJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
        }

        @DisplayName("Fail if one case payment from list cannot be removed as specified ID does not exist")
        @Test
        void shouldFailWithNotFoundWhenDeleteNonExistentId() throws Exception {

            List<CasePaymentOrderEntity> savedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(3);
            List<UUID> savedEntitiesUuids = savedEntities.stream()
                    .map(CasePaymentOrderEntity::getId)
                    .collect(Collectors.toList());
            assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());

            List<UUID> entitiesToDelete = new ArrayList<>(savedEntitiesUuids);

            entitiesToDelete.add(UUID.randomUUID());

            String[] savedEntitiesUuidsString = entitiesToDelete.stream().map(UUID::toString).toArray(String[]::new);

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS).queryParam(IDS, savedEntitiesUuidsString))
                    .andExpect(status().isNotFound());
            assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());
        }

        @DisplayName("Should delete entity and all its corresponding audit revisions")
        @Test
        void testIdPresentMultipleTimesForCreateAndUpdate() throws Exception {

            CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

            savedEntity.setAction("NewAction");
            casePaymentOrdersJpaRepository.saveAndFlush(savedEntity);

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS).queryParam(IDS, savedEntity.getId().toString()))
                    .andExpect(status().isOk());

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders?case-ids=")
    class DeleteCasePaymentOrdersByCaseIds {

        private static final String CASE_IDS = "case-ids";

        @DisplayName("Successfully delete single case payment order specified by an id")
        @Test
        void shouldDeleteSingleCasePaymentSpecifiedByCaseId() throws Exception {
            CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS).queryParam(CASE_IDS, savedEntity.getCaseId().toString()))
                    .andExpect(status().isOk());

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }

        @DisplayName("Successfully delete multiple case payments with specified ids")
        @Test
        void shouldDeleteMultipleCasePaymentsSpecifiedByCaseIds() throws Exception {
            List<CasePaymentOrderEntity> savedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(3);

            List<UUID> savedEntitiesUuids = savedEntities.stream()
                    .map(CasePaymentOrderEntity::getId)
                    .collect(Collectors.toList());

            List<String> savedEntitiesCaseIds = savedEntities.stream()
                    .map(CasePaymentOrderEntity::getCaseId)
                    .map(caseId -> Long.toString(caseId))
                    .collect(Collectors.toList());

            assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS)
                    .queryParam(CASE_IDS, savedEntitiesCaseIds.toArray(String[]::new)))
                    .andExpect(status().isOk());

            assertTrue(casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
            assertTrue(casePaymentOrdersAuditJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
        }

        @DisplayName("Fail if one case payment from list cannot be removed as specified Case Id does not exist")
        @Test
        void shouldFailWithNotFoundWhenDeleteNonExistentCaseId() throws Exception {

            List<CasePaymentOrderEntity> savedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(3);
            List<UUID> savedEntitiesUuids = savedEntities.stream()
                    .map(CasePaymentOrderEntity::getId)
                    .collect(Collectors.toList());
            assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());

            List<String> savedEntitiesCaseIds = savedEntities.stream()
                    .map(CasePaymentOrderEntity::getCaseId)
                    .map(caseId -> Long.toString(caseId))
                    .collect(Collectors.toList());
            savedEntitiesCaseIds.add(uidService.generateUID());

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS)
                    .queryParam(CASE_IDS, savedEntitiesCaseIds.toArray(String[]::new)))
                    .andExpect(status().isNotFound());
            assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());
        }

        @DisplayName("Should delete entity and all its corresponding audit revisions")
        @Test
        void testCaseIdPresentMultipleTimesForCreateAndUpdate() throws Exception {

            CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

            savedEntity.setAction("NewAction");
            casePaymentOrdersJpaRepository.saveAndFlush(savedEntity);

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS).queryParam(CASE_IDS, savedEntity.getCaseId().toString()))
                    .andExpect(status().isOk());

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }
    }
}
