package uk.gov.hmcts.reform.cpo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersAuditJpaRepository;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersJpaRepository;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;
import uk.gov.hmcts.reform.cpo.utils.UIDService;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;

public class CasePaymentOrdersControllerIT extends BaseTest {

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
    @DisplayName("POST /case-payment-orders")
    class CreateCasePaymentOrder {

        private final LocalDateTime effectiveFrom = LocalDateTime.of(2021, Month.MARCH, 24,
                11, 48, 32
        );
        private final Long caseId = 6_551_341_964_128_977L;
        private final String action = "action";
        private final String responsibleParty = "responsibleParty";
        private final String orderReference = "2021-11223344556";

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequest;

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequestInvalid;

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequestNull;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private CasePaymentOrdersJpaRepository repository;

        @BeforeEach
        void setUp() {

            repository.deleteAllInBatch();

            createCasePaymentOrderRequest = new CreateCasePaymentOrderRequest(effectiveFrom, "6551341964128977",
                    action, responsibleParty,
                    orderReference
            );
            createCasePaymentOrderRequestNull = new CreateCasePaymentOrderRequest(null, null,
                    null, null,
                    null
            );

            createCasePaymentOrderRequestInvalid = new CreateCasePaymentOrderRequest(effectiveFrom,
                    "655111964128977",
                    action, responsibleParty,
                    "2021-918425346"
            );

        }

        @DisplayName("Successfully created CasePaymentOrder")
        @Test
        void shouldSuccessfullyCreateCasePaymentOrder() throws Exception {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            this.mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest)))
                    .andExpect(jsonPath("$.created_timestamp").exists())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.case_id", is(caseId)))
                    .andExpect(jsonPath("$.action", is(action)))
                    .andExpect(jsonPath("$.responsible_party", is(responsibleParty)))
                    .andExpect(jsonPath("$.order_reference", is(orderReference)))
                    .andExpect(jsonPath("$.effective_from", is(effectiveFrom.format(formatter))))
                    .andExpect(jsonPath("$.created_by", is("445")));
        }

        @DisplayName("Null request fields throws errors")
        @Test
        void shouldThrowNotNullErrors() throws Exception {
            this.mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCasePaymentOrderRequestNull)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.message", is("Input not valid")))
                    .andExpect(jsonPath("$.details.length()", is(5)))
                    .andExpect(jsonPath("$.details", hasItem(ValidationError.ACTION_REQUIRED)))
                    .andExpect(jsonPath("$.details", hasItem(ValidationError.ORDER_REFERENCE_REQUIRED)))
                    .andExpect(jsonPath("$.details", hasItem(ValidationError.CASE_ID_REQUIRED)))
                    .andExpect(jsonPath("$.details", hasItem(ValidationError.EFFECTIVE_FROM_REQUIRED)))
                    .andExpect(jsonPath("$.details", hasItem(ValidationError.RESPONSIBLE_PARTY_REQUIRED)));
        }

        @DisplayName("Invalid request fields throws errors")
        @Test
        void shouldThrowInvalidFormErrors() throws Exception {
            this.mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCasePaymentOrderRequestInvalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.message", is("Input not valid")))
                    .andExpect(jsonPath("$.details.length()", is(3)))
                    .andExpect(jsonPath("$.details", hasItem(ValidationError.CASE_ID_INVALID_LENGTH)))
                    .andExpect(jsonPath("$.details", hasItem(ValidationError.CASE_ID_INVALID)))
                    .andExpect(jsonPath("$.details", hasItem(ValidationError.ORDER_REFERENCE_INVALID)));
        }

        @DisplayName("Non-unique order reference and case id pairing throws errors")
        @Test
        void shouldThrowNonUniquePairingErrors() throws Exception {
            this.mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest)))
                    .andExpect(status().isCreated());

            this.mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", is(ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE)));
        }

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
                    .andExpect(status().isNoContent());

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
                    .andExpect(status().isNoContent());

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
                    .andExpect(jsonPath("$.message", is(ValidationError.CPO_NOT_FOUND_BY_ID)));
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
                    .andExpect(status().isNoContent());

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }

        @DisplayName("Should fail with 400 Bad Request when invalid id (not a UUID) specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidUuidIsSpecified() throws Exception {
            final String invalidUuid = "123";
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(IDS, invalidUuid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message",
                            containsString("deleteCasePaymentOrdersById.ids: These ids: "
                                    + invalidUuid
                                    + " are incorrect.")));
        }

        @DisplayName("Should fail with 400 Bad Request when CaseIds and UUIDs are specified")
        @Test
        void shouldThrow400BadRequestWheUuidsAndCaseIdsAreSpecified() throws Exception {
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                    .queryParam(IDS, UUID.randomUUID().toString())
                    .queryParam(CASE_IDS, uidService.generateUID()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message",
                            is(ValidationError.CANNOT_DELETE_USING_IDS_AND_CASE_IDS)));
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
                    .andExpect(status().isNoContent());

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
                    .andExpect(status().isNoContent());

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
                    .andExpect(status().isNoContent());

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
                            is(ValidationError.CPO_NOT_FOUND_BY_CASE_ID)));


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
                    .andExpect(status().isNoContent());

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }

        @DisplayName("Should fail with 400 Bad Request when invalid length caseId is specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidLengthCaseIdSpecified() throws Exception {
            final String invalidCaseId = "12345";
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(CASE_IDS, invalidCaseId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("These caseIDs: "
                            + invalidCaseId
                            + " are incorrect")));
        }

        @DisplayName("Should fail with 400 Bad Request when invalid caseId is specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidCaseIdSpecified() throws Exception {
            final String invalidLuhn = "1234567890123456";
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).queryParam(CASE_IDS, invalidLuhn))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("These caseIDs: "
                            + invalidLuhn
                            + " are incorrect")));
        }
    }
}
