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
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;

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

    private static final String CASE_IDS = "case-ids";

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
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(IDS, savedEntity.getId().toString()))
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

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(IDS, savedEntitiesUuidsString))
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

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(IDS, savedEntitiesUuidsString))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("One of the supplied UUID's was not present in the database")));
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

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(IDS, savedEntity.getId().toString()))
                    .andExpect(status().isOk());

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }

        @DisplayName("Should fail with 400 Bad Request when invalid id (not a UUID) specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidUuidIsSpecified() throws Exception {
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(IDS, "123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message",
                            containsString("Failed to convert value of type 'java.lang.String'"
                                    + " to required type 'java.util.List'")))
                    .andExpect(jsonPath("$.details.message", is("Invalid UUID string: 123")));
        }

        @DisplayName("Should fail with 400 Bad Request when empty list of UUID's are specified")
        @Test
        void shouldThrow400BadRequestWhenEmptyListOfUuidsIsSpecified() throws Exception {
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(IDS, ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("IDs are required")));

        }

        @DisplayName("Should fail with 400 Bad Request when CaseIds and UUIDs are specified")
        @Test
        void shouldThrow400BadRequestWheUuidsAndCaseIdsAreSpecified() throws Exception {
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                    .queryParam(IDS, UUID.randomUUID().toString())
                    .queryParam(CASE_IDS, uidService.generateUID()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Can't delete case payment order with "
                            + "both id AND case-id specified")));
        }
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders?case-ids=")
    class DeleteCasePaymentOrdersByCaseIds {

        @DisplayName("Successfully delete single case payment order specified by an id")
        @Test
        void shouldDeleteSingleCasePaymentSpecifiedByCaseId() throws Exception {
            CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(CASE_IDS, savedEntity.getCaseId().toString()))
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

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                    .queryParam(CASE_IDS, savedEntitiesCaseIds.toArray(String[]::new)))
                    .andExpect(status().isOk());

            assertTrue(casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
            assertTrue(casePaymentOrdersAuditJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
        }

        @DisplayName("Successfully delete multiple case payment orders with the same case id")
        @Test
        void shouldDeleteMultipleCasePaymentsSpecifiedByTheSameCaseId() throws Exception {
            List<CasePaymentOrderEntity> savedEntities =
                    casePaymentOrderEntityGenerator.generateAndSaveEntitiesWithSameCaseId(3);

            List<UUID> savedEntitiesUuids = savedEntities.stream()
                    .map(CasePaymentOrderEntity::getId)
                    .collect(Collectors.toList());

            String savedEntityCaseId = savedEntities.stream()
                    .map(CasePaymentOrderEntity::getCaseId)
                    .map(caseId -> Long.toString(caseId))
                    .distinct()
                    .collect(Collectors.joining());

            assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                    .queryParam(CASE_IDS, savedEntityCaseId))
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

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                    .queryParam(CASE_IDS, savedEntitiesCaseIds.toArray(String[]::new)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message",
                            is("One of the supplied Case ID's was not present in the database")));


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

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(CASE_IDS, savedEntity.getCaseId().toString()))
                    .andExpect(status().isOk());

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }

        @DisplayName("Should fail with 400 Bad Request when invalid length caseId is specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidLengthCaseIdSpecified() throws Exception {
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(CASE_IDS, "12345"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString(ValidationError.CASE_ID_INVALID_LENGTH)));
        }

        @DisplayName("Should fail with 400 Bad Request when invalid caseId is specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidCaseIdSpecified() throws Exception {
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(CASE_IDS, "1234567890123456"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString(ValidationError.CASE_ID_INVALID)));
        }

        @DisplayName("Should fail with 400 Bad Request when empty list of Case-ID's are specified")
        @Test
        void shouldThrow400BadRequestWhenEmptyListOfCaseIdsIsSpecified() throws Exception {
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(CASE_IDS, ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString(ValidationError.CASE_IDS_EMPTY)));
        }
    }
}
