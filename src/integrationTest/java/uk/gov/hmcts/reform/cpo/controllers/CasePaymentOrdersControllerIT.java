package uk.gov.hmcts.reform.cpo.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.auditlog.AuditOperationType;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrderAuditRevision;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersAuditJpaRepository;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersJpaRepository;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderAuditUtils;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;
import uk.gov.hmcts.reform.cpo.utils.UIDService;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_IDS;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.IDS;

class CasePaymentOrdersControllerIT extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CasePaymentOrdersAuditJpaRepository casePaymentOrdersAuditJpaRepository;

    @Autowired
    private CasePaymentOrdersJpaRepository casePaymentOrdersJpaRepository;

    @Autowired
    private CasePaymentOrderAuditUtils auditUtils;

    @Autowired
    private CasePaymentOrderEntityGenerator casePaymentOrderEntityGenerator;

    @Autowired
    private UIDService uidService;

    @BeforeEach
    @Transactional
    public void setUp() {
        casePaymentOrdersJpaRepository.deleteAllInBatch();
        casePaymentOrdersAuditJpaRepository.deleteAllInBatch();
    }


    @Nested
    @DisplayName("POST /case-payment-orders")
    class CreateCasePaymentOrder {

        private String caseId;

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequest;

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequestInvalid;

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequestNull;

        @BeforeEach
        void setUp() {

            caseId = uidService.generateUID();

            createCasePaymentOrderRequest = new CreateCasePaymentOrderRequest(caseId,
                                                                              ACTION, RESPONSIBLE_PARTY,
                                                                              ORDER_REFERENCE_VALID
            );

            createCasePaymentOrderRequestNull = new CreateCasePaymentOrderRequest(null,
                                                                                  null, null,
                                                                                  null
            );

            createCasePaymentOrderRequestInvalid = new CreateCasePaymentOrderRequest(CASE_ID_INVALID,
                                                                                     ACTION, RESPONSIBLE_PARTY,
                                                                                     ORDER_REFERENCE_INVALID
            );

        }


        @Nested
        @DisplayName("Negative Authentication Tests (GET)")
        class NegativeAuthTest implements BaseMvcAuthChecks {

            private MockHttpServletRequestBuilder happyPathRequestBuilder;

            @BeforeEach
            public void setUp() throws JsonProcessingException {
                // NB: need a unique case ID when running happy path for a second time.
                caseId = uidService.generateUID();
                createCasePaymentOrderRequest = new CreateCasePaymentOrderRequest(caseId,
                                                                                  ACTION, RESPONSIBLE_PARTY,
                                                                                  ORDER_REFERENCE_VALID
                );

                happyPathRequestBuilder = post(CASE_PAYMENT_ORDERS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest));
            }

            @Test
            @Override
            @DisplayName(DISPLAY_ALL_AUTH_OK)
            public void should2xxSuccessfulForHappyPath() throws Exception {
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
                setUp(); // re-run setup to ensure CPO details are unique for second test call
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_CREATE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MISSING)
            public void should401ForMissingAuthToken() throws Exception {
                assert401ForMissingAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MALFORMED)
            public void should401ForMalformedAuthToken() throws Exception {
                assert401ForMalformedAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_EXPIRED)
            public void should401ForExpiredAuthToken() throws Exception {
                assert401ForExpiredAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MISSING)
            public void should401ForMissingServiceAuthToken() throws Exception {
                assert401ForMissingServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MALFORMED)
            public void should401ForMalformedServiceAuthToken() throws Exception {
                assert401ForMalformedServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_EXPIRED)
            public void should401ForExpiredServiceAuthToken() throws Exception {
                assert401ForExpiredServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_UNAUTHORISED)
            public void should403ForUnauthorisedServiceAuthToken() throws Exception {
                assert403ForUnauthorisedServiceAuthToken(mockMvc, happyPathRequestBuilder);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_PERMISSION_MISSING)
            public void should403ForServiceMissingS2sPermission() throws Exception {
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_READ_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_UPDATE_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_DELETE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfAuthServiceUnavailable() throws Exception {
                assert401IfIdamUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfS2sAuthServiceUnavailable() throws Exception {
                assert401IfS2sAuthServiceUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

        }

        @DisplayName("Successfully created CasePaymentOrder")
        @Test
        void shouldSuccessfullyCreateCasePaymentOrder() throws Exception {
            LocalDateTime beforeCreateTimestamp = LocalDateTime.now();

            MvcResult result =
                mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                                    .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.case_id", is(Long.valueOf(caseId))))
                    .andExpect(jsonPath("$.action", is(ACTION)))
                    .andExpect(jsonPath("$.responsible_party", is(RESPONSIBLE_PARTY)))
                    .andExpect(jsonPath("$.order_reference", is(ORDER_REFERENCE_VALID)))
                    .andExpect(jsonPath("$.created_by", is(IDAM_MOCK_USER_ID)))
                    .andExpect(jsonPath("$.created_timestamp").exists())
                    .andExpect(jsonPath("$.history_exists", is(HISTORY_EXISTS_DEFAULT)))
                .andReturn();

            UUID id = UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText()
            );
            verifyDbCpoValues(id, createCasePaymentOrderRequest, beforeCreateTimestamp);
            verifyDbCpoAuditValues(id, createCasePaymentOrderRequest, beforeCreateTimestamp);
            verifyLogAuditValues(result,
                                 AuditOperationType.CREATE_CASE_PAYMENT_ORDER,
                                 AUTHORISED_CRUD_SERVICE,
                                 id.toString(),
                                 createCasePaymentOrderRequest.getCaseId());
        }

        @DisplayName("Null request fields throws errors")
        @Test
        void shouldThrowNotNullErrors() throws Exception {
            mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createCasePaymentOrderRequestNull)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath(ERROR_PATH_MESSAGE, is(ValidationError.ARGUMENT_NOT_VALID)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasSize(4)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.ACTION_REQUIRED)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.ORDER_REFERENCE_REQUIRED)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.CASE_ID_REQUIRED)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.RESPONSIBLE_PARTY_REQUIRED)))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.CREATE_CASE_PAYMENT_ORDER,
                                                AUTHORISED_CRUD_SERVICE,
                                                null,
                                                createCasePaymentOrderRequest.getCaseId()));
        }

        @DisplayName("Invalid request fields throws errors")
        @Test
        void shouldThrowInvalidFormErrors() throws Exception {
            mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createCasePaymentOrderRequestInvalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath(ERROR_PATH_MESSAGE, is(ValidationError.ARGUMENT_NOT_VALID)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasSize(2)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.CASE_ID_INVALID)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.ORDER_REFERENCE_INVALID)))
                .andExpect(hasGeneratedLogAudit(AuditOperationType.CREATE_CASE_PAYMENT_ORDER,
                                                AUTHORISED_CRUD_SERVICE,
                                                null,
                                                createCasePaymentOrderRequest.getCaseId()));
        }

        @DisplayName("Non-unique order reference and case id pairing throws errors")
        @Test
        void shouldThrowNonUniquePairingErrors() throws Exception {

            // GIVEN
            // generate and save first entity
            CasePaymentOrderEntity savedEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);

            // make a request with matching Case-ID and OrderReference
            createCasePaymentOrderRequest = new CreateCasePaymentOrderRequest(savedEntity.getCaseId().toString(),
                                                                              ACTION,
                                                                              RESPONSIBLE_PARTY,
                                                                              savedEntity.getOrderReference());

            mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath(ERROR_PATH_MESSAGE, is(ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE)))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.CREATE_CASE_PAYMENT_ORDER,
                                                AUTHORISED_CRUD_SERVICE,
                                                null,
                                                createCasePaymentOrderRequest.getCaseId()));
        }

        private void verifyDbCpoValues(UUID id,
                                       CreateCasePaymentOrderRequest request,
                                       LocalDateTime beforeCreateTimestamp) {
            Optional<CasePaymentOrderEntity> savedEntity = casePaymentOrdersJpaRepository.findById(id);

            assertTrue(savedEntity.isPresent());
            assertEquals(id, savedEntity.get().getId());
            assertEquals(request.getCaseId(), savedEntity.get().getCaseId().toString());
            assertEquals(request.getAction(), savedEntity.get().getAction());
            assertEquals(request.getResponsibleParty(), savedEntity.get().getResponsibleParty());
            assertEquals(request.getOrderReference(), savedEntity.get().getOrderReference());
            assertEquals(IDAM_MOCK_USER_ID, savedEntity.get().getCreatedBy());
            assertNotNull(savedEntity.get().getCreatedTimestamp());
            assertTrue(beforeCreateTimestamp.isBefore(savedEntity.get().getCreatedTimestamp()));
            assertFalse(savedEntity.get().isHistoryExists());
        }

        private void verifyDbCpoAuditValues(UUID id,
                                            CreateCasePaymentOrderRequest request,
                                            LocalDateTime beforeCreateTimestamp) {
            // get latest
            CasePaymentOrderAuditRevision latestRevision = getLatestAuditRevision(id);
            // expecting a create
            assertEquals(RevisionType.ADD, latestRevision.getRevisionType());
            // verify content
            assertEquals(id, latestRevision.getEntity().getId());
            assertEquals(request.getCaseId(), latestRevision.getEntity().getCaseId().toString());
            assertEquals(request.getAction(), latestRevision.getEntity().getAction());
            assertEquals(request.getResponsibleParty(), latestRevision.getEntity().getResponsibleParty());
            assertEquals(request.getOrderReference(), latestRevision.getEntity().getOrderReference());
            assertEquals(IDAM_MOCK_USER_ID, latestRevision.getEntity().getCreatedBy());
            assertNotNull(latestRevision.getEntity().getCreatedTimestamp());
            assertTrue(beforeCreateTimestamp.isBefore(latestRevision.getEntity().getCreatedTimestamp()));
            assertFalse(latestRevision.getEntity().isHistoryExists());
        }
    }


    @Nested
    @DisplayName("DELETE /case-payment-orders?ids=")
    class DeleteCasePaymentOrdersByIds {

        @Nested
        @DisplayName("Negative Authentication Tests (DELETE?ids=)")
        class NegativeAuthTest implements BaseMvcAuthChecks {

            private MockHttpServletRequestBuilder happyPathRequestBuilder;

            @BeforeEach
            public void setUp() {
                CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
                assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

                happyPathRequestBuilder =  delete(CASE_PAYMENT_ORDERS_PATH)
                                    .queryParam(IDS, savedEntity.getId().toString());
            }

            @Test
            @Override
            @DisplayName(DISPLAY_ALL_AUTH_OK)
            public void should2xxSuccessfulForHappyPath() throws Exception {
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
                setUp(); // re-run setup to create a new CPO to delete
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_DELETE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MISSING)
            public void should401ForMissingAuthToken() throws Exception {
                assert401ForMissingAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MALFORMED)
            public void should401ForMalformedAuthToken() throws Exception {
                assert401ForMalformedAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_EXPIRED)
            public void should401ForExpiredAuthToken() throws Exception {
                assert401ForExpiredAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MISSING)
            public void should401ForMissingServiceAuthToken() throws Exception {
                assert401ForMissingServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MALFORMED)
            public void should401ForMalformedServiceAuthToken() throws Exception {
                assert401ForMalformedServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_EXPIRED)
            public void should401ForExpiredServiceAuthToken() throws Exception {
                assert401ForExpiredServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_UNAUTHORISED)
            public void should403ForUnauthorisedServiceAuthToken() throws Exception {
                assert403ForUnauthorisedServiceAuthToken(mockMvc, happyPathRequestBuilder);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_PERMISSION_MISSING)
            public void should403ForServiceMissingS2sPermission() throws Exception {
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_CREATE_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_READ_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_UPDATE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfAuthServiceUnavailable() throws Exception {
                assert401IfIdamUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfS2sAuthServiceUnavailable() throws Exception {
                assert401IfS2sAuthServiceUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

        }

        @DisplayName("Successfully delete single case payment order specified by an id")
        @Test
        void shouldDeleteSingleCasePaymentSpecifiedById() throws Exception {

            CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(IDS, savedEntity.getId().toString()))
                    .andExpect(status().isNoContent())

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    savedEntity.getId().toString(),
                                                    null));

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

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(IDS, savedEntitiesUuidsString))
                    .andExpect(status().isNoContent())

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    Arrays.asList(savedEntitiesUuidsString),
                                                    null));

            assertTrue(casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
            assertTrue(casePaymentOrdersAuditJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
        }

        @DisplayName("Fail if one case payment from list cannot be removed as specified ID does not exist")
        @Test
        void shouldFailWithNotFoundWhenDeleteNonExistentId() throws Exception {
            assert404ResponseWhenSpecifiedIdentifierNotFound(HttpMethod.DELETE, IDS);
        }

        @DisplayName("Should delete entity and all its corresponding audit revisions")
        @Test
        void testIdPresentMultipleTimesForCreateAndUpdate() throws Exception {

            CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

            savedEntity.setAction("NewAction");
            casePaymentOrdersJpaRepository.saveAndFlush(savedEntity);

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(IDS, savedEntity.getId().toString()))
                    .andExpect(status().isNoContent())

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    savedEntity.getId().toString(),
                                                    null));

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }

        @DisplayName("Should fail with 400 Bad Request when invalid id (not a UUID) specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidUuidIsSpecified() throws Exception {
            final String invalidUuid = "123";
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(IDS, invalidUuid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(ERROR_PATH_MESSAGE,
                            containsString("deleteCasePaymentOrdersById.ids: These ids: "
                                    + invalidUuid
                                    + " are incorrect.")))

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    invalidUuid,
                                                    null));
        }

        @DisplayName("Should fail with 400 Bad Request when CaseIds and UUIDs are specified")
        @Test
        void shouldThrow400BadRequestWheUuidsAndCaseIdsAreSpecified() throws Exception {
            String cpoId = UUID.randomUUID().toString();
            String caseId = uidService.generateUID();

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(IDS, cpoId)
                                .queryParam(CASE_IDS, caseId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(ERROR_PATH_MESSAGE,
                            is(ValidationError.CANNOT_DELETE_USING_IDS_AND_CASE_IDS)))

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    cpoId,
                                                    caseId));
        }
    }


    @Nested
    @DisplayName("DELETE /case-payment-orders?case_ids=")
    class DeleteCasePaymentOrdersByCaseIds {

        @Nested
        @DisplayName("Negative Authentication Tests (DELETE?case_ids=)")
        class NegativeAuthTest implements BaseMvcAuthChecks {

            private MockHttpServletRequestBuilder happyPathRequestBuilder;

            @BeforeEach
            public void setUp() {
                CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
                assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

                happyPathRequestBuilder = delete(CASE_PAYMENT_ORDERS_PATH)
                    .queryParam(CASE_IDS, savedEntity.getCaseId().toString());
            }

            @Test
            @Override
            @DisplayName(DISPLAY_ALL_AUTH_OK)
            public void should2xxSuccessfulForHappyPath() throws Exception {
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
                setUp();  // re-run setup to create a new CPO to delete
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_DELETE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MISSING)
            public void should401ForMissingAuthToken() throws Exception {
                assert401ForMissingAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MALFORMED)
            public void should401ForMalformedAuthToken() throws Exception {
                assert401ForMalformedAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_EXPIRED)
            public void should401ForExpiredAuthToken() throws Exception {
                assert401ForExpiredAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MISSING)
            public void should401ForMissingServiceAuthToken() throws Exception {
                assert401ForMissingServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MALFORMED)
            public void should401ForMalformedServiceAuthToken() throws Exception {
                assert401ForMalformedServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_EXPIRED)
            public void should401ForExpiredServiceAuthToken() throws Exception {
                assert401ForExpiredServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_UNAUTHORISED)
            public void should403ForUnauthorisedServiceAuthToken() throws Exception {
                assert403ForUnauthorisedServiceAuthToken(mockMvc, happyPathRequestBuilder);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_PERMISSION_MISSING)
            public void should403ForServiceMissingS2sPermission() throws Exception {
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_CREATE_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_READ_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_UPDATE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfAuthServiceUnavailable() throws Exception {
                assert401IfIdamUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfS2sAuthServiceUnavailable() throws Exception {
                assert401IfS2sAuthServiceUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

        }

        @DisplayName("Successfully delete single case payment order specified by an id")
        @Test
        void shouldDeleteSingleCasePaymentSpecifiedByCaseId() throws Exception {
            CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(CASE_IDS, savedEntity.getCaseId().toString()))
                    .andExpect(status().isNoContent())

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    null,
                                                    savedEntity.getCaseId().toString()));

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
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(CASE_IDS, savedEntitiesCaseIds.toArray(String[]::new)))
                    .andExpect(status().isNoContent())

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    null,
                                                    savedEntitiesCaseIds));

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
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(CASE_IDS, savedEntityCaseId))
                    .andExpect(status().isNoContent())

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    null,
                                                    savedEntityCaseId));

            assertTrue(casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
            assertTrue(casePaymentOrdersAuditJpaRepository.findAllById(savedEntitiesUuids).isEmpty());
        }

        @DisplayName("Should delete entity and all its corresponding audit revisions")
        @Test
        void testCaseIdPresentMultipleTimesForCreateAndUpdate() throws Exception {

            CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

            savedEntity.setAction("NewAction");
            casePaymentOrdersJpaRepository.saveAndFlush(savedEntity);

            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(CASE_IDS, savedEntity.getCaseId().toString()))
                    .andExpect(status().isNoContent())

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    null,
                                                    savedEntity.getCaseId().toString()));

            assertFalse(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());
            assertFalse(casePaymentOrdersAuditJpaRepository.findById(savedEntity.getId()).isPresent());
        }

        @DisplayName("Should fail with 400 Bad Request when invalid length caseId is specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidLengthCaseIdSpecified() throws Exception {
            final String invalidCaseId = "12345";
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(CASE_IDS, invalidCaseId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(ERROR_PATH_MESSAGE, containsString("These caseIDs: "
                            + invalidCaseId
                            + " are incorrect")))

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    null,
                                                    invalidCaseId));
        }

        @DisplayName("Should fail with 400 Bad Request when invalid caseId is specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidCaseIdSpecified() throws Exception {
            final String invalidLuhn = "1234567890123456";
            mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                .queryParam(CASE_IDS, invalidLuhn))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(ERROR_PATH_MESSAGE, containsString("These caseIDs: "
                            + invalidLuhn
                            + " are incorrect")))

                    .andExpect(hasGeneratedLogAudit(AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
                                                    AUTHORISED_CRUD_SERVICE,
                                                    null,
                                                    invalidLuhn));
        }

        @DisplayName("Should fail with 404 Not Found if any of the specified CaseIds cannot be found in database")
        @Test
        void shouldThrow404NotFoundWhenCaseIdSpecifiedNotFound() throws Exception {
            assert404ResponseWhenSpecifiedIdentifierNotFound(HttpMethod.DELETE, CASE_IDS);
        }
    }


    @Nested
    @DisplayName("GET /case-payment-orders?ids=")
    class GetCasePaymentOrdersByIds {

        @Nested
        @DisplayName("Negative Authentication Tests (GET?ids=)")
        class NegativeAuthTest implements BaseMvcAuthChecks {

            private MockHttpServletRequestBuilder happyPathRequestBuilder;

            @BeforeEach
            public void setUp() {
                CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
                assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

                happyPathRequestBuilder = get(CASE_PAYMENT_ORDERS_PATH)
                    .queryParam(IDS, savedEntity.getId().toString());
            }

            @Test
            @Override
            @DisplayName(DISPLAY_ALL_AUTH_OK)
            public void should2xxSuccessfulForHappyPath() throws Exception {
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_READ_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MISSING)
            public void should401ForMissingAuthToken() throws Exception {
                assert401ForMissingAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MALFORMED)
            public void should401ForMalformedAuthToken() throws Exception {
                assert401ForMalformedAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_EXPIRED)
            public void should401ForExpiredAuthToken() throws Exception {
                assert401ForExpiredAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MISSING)
            public void should401ForMissingServiceAuthToken() throws Exception {
                assert401ForMissingServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MALFORMED)
            public void should401ForMalformedServiceAuthToken() throws Exception {
                assert401ForMalformedServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_EXPIRED)
            public void should401ForExpiredServiceAuthToken() throws Exception {
                assert401ForExpiredServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_UNAUTHORISED)
            public void should403ForUnauthorisedServiceAuthToken() throws Exception {
                assert403ForUnauthorisedServiceAuthToken(mockMvc, happyPathRequestBuilder);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_PERMISSION_MISSING)
            public void should403ForServiceMissingS2sPermission() throws Exception {
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_CREATE_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_UPDATE_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_DELETE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfAuthServiceUnavailable() throws Exception {
                assert401IfIdamUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfS2sAuthServiceUnavailable() throws Exception {
                assert401IfS2sAuthServiceUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

        }

        @DisplayName("Successfully get single case payment order specified by an id")
        @Test
        void shouldGetSingleCasePaymentSpecifiedById() throws Exception {

            // GIVEN
            CasePaymentOrderEntity savedEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

            // WHEN
            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE))
                                .queryParam(IDS, savedEntity.getId().toString()))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(savedEntity.getId().toString())))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                savedEntity.getId().toString(),
                                                null));

        }

        @DisplayName("Successfully get multiple case payments with specified ids")
        @Test
        void shouldGetMultipleCasePaymentsSpecifiedByIds() throws Exception {

            // GIVEN
            List<CasePaymentOrderEntity> savedEntities
                = casePaymentOrderEntityGenerator.generateAndSaveEntities(3);
            List<UUID> savedEntitiesUuids = savedEntities.stream()
                .map(CasePaymentOrderEntity::getId)
                .collect(Collectors.toList());
            assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());

            String[] savedEntitiesUuidsString = savedEntitiesUuids.stream().map(UUID::toString).toArray(String[]::new);

            // WHEN
            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE))
                                .queryParam(IDS, savedEntitiesUuidsString))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(savedEntitiesUuidsString[0])))
                .andExpect(jsonPath("$.content[*].id", hasItem(savedEntitiesUuidsString[1])))
                .andExpect(jsonPath("$.content[*].id", hasItem(savedEntitiesUuidsString[2])))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                Arrays.asList(savedEntitiesUuidsString),
                                                null));
        }

        @DisplayName("Should fail with 400 Bad Request when invalid id (not a UUID) specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidUuidIsSpecified() throws Exception {

            // GIVEN
            final String invalidUuid = "123";

            // WHEN
            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE))
                                .queryParam(IDS, invalidUuid))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_PATH_MESSAGE,
                                    containsString("getCasePaymentOrders.ids: These ids: "
                                                       + invalidUuid
                                                       + " are incorrect.")))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                invalidUuid,
                                                null));
        }

        @DisplayName("Should fail with 404 Not Found if any of the specified ids cannot be found in database")
        @Test
        void shouldThrow404NotFoundWhenUuidSpecifiedNotFound() throws Exception {
            assert404ResponseWhenSpecifiedIdentifierNotFound(HttpMethod.GET, IDS);
        }

        @DisplayName("Should fail with 400 Bad Request when CaseIds and UUIDs are specified")
        @Test
        void shouldThrow400BadRequestWheUuidsAndCaseIdsAreSpecified() throws Exception {

            // GIVEN
            String cpoId = UUID.randomUUID().toString();
            String caseId = uidService.generateUID();

            // WHEN
            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE))
                                .queryParam(IDS, cpoId)
                                .queryParam(CASE_IDS, caseId))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_PATH_MESSAGE,
                                    is(ValidationError.CPO_FILTER_ERROR)))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                cpoId,
                                                caseId));
        }

        @DisplayName("Should return empty results if neither CaseIds nor UUIDs are specified")
        @Test
        void shouldReturnEmptyResultIfNeitherUuidsNorCaseIdsAreSpecified() throws Exception {

            // WHEN
            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                (String)null,
                                                null));
        }
    }


    @Nested
    @DisplayName("GET /case-payment-orders?case_ids=")
    class GetCasePaymentOrdersByCaseIds {

        @Nested
        @DisplayName("Negative Authentication Tests (GET?case_ids=)")
        class NegativeAuthTest implements BaseMvcAuthChecks {

            private MockHttpServletRequestBuilder happyPathRequestBuilder;

            @BeforeEach
            public void setUp() {
                CasePaymentOrderEntity savedEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
                assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

                happyPathRequestBuilder = get(CASE_PAYMENT_ORDERS_PATH)
                    .queryParam(CASE_IDS, savedEntity.getCaseId().toString());
            }

            @Test
            @Override
            @DisplayName(DISPLAY_ALL_AUTH_OK)
            public void should2xxSuccessfulForHappyPath() throws Exception {
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_READ_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MISSING)
            public void should401ForMissingAuthToken() throws Exception {
                assert401ForMissingAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MALFORMED)
            public void should401ForMalformedAuthToken() throws Exception {
                assert401ForMalformedAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_EXPIRED)
            public void should401ForExpiredAuthToken() throws Exception {
                assert401ForExpiredAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MISSING)
            public void should401ForMissingServiceAuthToken() throws Exception {
                assert401ForMissingServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MALFORMED)
            public void should401ForMalformedServiceAuthToken() throws Exception {
                assert401ForMalformedServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_UNAUTHORISED)
            public void should403ForUnauthorisedServiceAuthToken() throws Exception {
                assert403ForUnauthorisedServiceAuthToken(mockMvc, happyPathRequestBuilder);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_EXPIRED)
            public void should401ForExpiredServiceAuthToken() throws Exception {
                assert401ForExpiredServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_PERMISSION_MISSING)
            public void should403ForServiceMissingS2sPermission() throws Exception {
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_CREATE_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_UPDATE_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_DELETE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfAuthServiceUnavailable() throws Exception {
                assert401IfIdamUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfS2sAuthServiceUnavailable() throws Exception {
                assert401IfS2sAuthServiceUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

        }

        @DisplayName("Successfully get single case payment order specified by an id")
        @Test
        void shouldGetSingleCasePaymentSpecifiedByCaseId() throws Exception {

            // GIVEN
            CasePaymentOrderEntity savedEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            assertTrue(casePaymentOrdersJpaRepository.findById(savedEntity.getId()).isPresent());

            // WHEN
            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE))
                                .queryParam(CASE_IDS, savedEntity.getCaseId().toString()))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(savedEntity.getId().toString())))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                null,
                                                savedEntity.getCaseId().toString()));
        }

        @DisplayName("Successfully get multiple case payments with specified ids")
        @Test
        void shouldGetMultipleCasePaymentsSpecifiedByCaseIds() throws Exception {

            // GIVEN
            List<CasePaymentOrderEntity> savedEntities =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(3);

            List<UUID> savedEntitiesUuids = savedEntities.stream()
                .map(CasePaymentOrderEntity::getId)
                .collect(Collectors.toList());

            List<String> savedEntitiesCaseIds = savedEntities.stream()
                .map(CasePaymentOrderEntity::getCaseId)
                .map(caseId -> Long.toString(caseId))
                .collect(Collectors.toList());

            // WHEN
            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE))
                                .queryParam(CASE_IDS, savedEntitiesCaseIds.toArray(String[]::new)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(savedEntitiesUuids.get(0).toString())))
                .andExpect(jsonPath("$.content[*].id", hasItem(savedEntitiesUuids.get(1).toString())))
                .andExpect(jsonPath("$.content[*].id", hasItem(savedEntitiesUuids.get(2).toString())))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                null,
                                                savedEntitiesCaseIds));
        }

        @DisplayName("Successfully get multiple case payment orders with the same case id")
        @Test
        void shouldGetMultipleCasePaymentsSpecifiedByTheSameCaseId() throws Exception {
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

            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE))
                                .queryParam(CASE_IDS, savedEntityCaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(savedEntitiesUuids.get(0).toString())))
                .andExpect(jsonPath("$.content[*].id", hasItem(savedEntitiesUuids.get(1).toString())))
                .andExpect(jsonPath("$.content[*].id", hasItem(savedEntitiesUuids.get(2).toString())))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                null,
                                                savedEntityCaseId));
        }

        @DisplayName("Should fail with 400 Bad Request when invalid length caseId is specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidLengthCaseIdSpecified() throws Exception {

            // GIVEN
            final String invalidCaseId = "12345";

            // WHEN
            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE))
                                .queryParam(CASE_IDS, invalidCaseId))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_PATH_MESSAGE, containsString("These caseIDs: "
                                                                           + invalidCaseId
                                                                           + " are incorrect")))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                null,
                                                invalidCaseId));
        }

        @DisplayName("Should fail with 400 Bad Request when invalid caseId is specified")
        @Test
        void shouldThrow400BadRequestWhenInvalidCaseIdSpecified() throws Exception {

            // GIVEN
            final String invalidLuhn = "1234567890123456";

            // WHEN
            mockMvc.perform(get(CASE_PAYMENT_ORDERS_PATH)
                                .headers(createHttpHeaders(AUTHORISED_READ_SERVICE))
                                .queryParam(CASE_IDS, invalidLuhn))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_PATH_MESSAGE, containsString("These caseIDs: "
                                                                           + invalidLuhn
                                                                           + " are incorrect")))

                .andExpect(hasGeneratedLogAudit(AuditOperationType.GET_CASE_PAYMENT_ORDER,
                                                AUTHORISED_READ_SERVICE,
                                                null,
                                                invalidLuhn));
        }

        @DisplayName("Should fail with 404 Not Found if any of the specified CaseIds cannot be found in database")
        @Test
        void shouldThrow404NotFoundWhenCaseIdSpecifiedNotFound() throws Exception {
            assert404ResponseWhenSpecifiedIdentifierNotFound(HttpMethod.GET, CASE_IDS);
        }
    }


    @Nested
    @DisplayName("PUT /case-payment-orders")
    class UpdateCasePaymentOrder {

        @Nested
        @DisplayName("Negative Authentication Tests (PUT)")
        class NegativeAuthTest implements BaseMvcAuthChecks {

            private MockHttpServletRequestBuilder happyPathRequestBuilder;

            @BeforeEach
            public void setUp() throws JsonProcessingException {
                CasePaymentOrderEntity originalEntity =
                    casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
                UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                    originalEntity.getId().toString(),
                    uidService.generateUID(),
                    ACTION,
                    RESPONSIBLE_PARTY,
                    ORDER_REFERENCE_VALID
                );

                happyPathRequestBuilder = put(CASE_PAYMENT_ORDERS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request));
            }

            @Test
            @Override
            @DisplayName(DISPLAY_ALL_AUTH_OK)
            public void should2xxSuccessfulForHappyPath() throws Exception {
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
                assert2xxSuccessfulForHappyPath(mockMvc, happyPathRequestBuilder, AUTHORISED_UPDATE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MISSING)
            public void should401ForMissingAuthToken() throws Exception {
                assert401ForMissingAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_MALFORMED)
            public void should401ForMalformedAuthToken() throws Exception {
                assert401ForMalformedAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_EXPIRED)
            public void should401ForExpiredAuthToken() throws Exception {
                assert401ForExpiredAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MISSING)
            public void should401ForMissingServiceAuthToken() throws Exception {
                assert401ForMissingServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_MALFORMED)
            public void should401ForMalformedServiceAuthToken() throws Exception {
                assert401ForMalformedServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_EXPIRED)
            public void should401ForExpiredServiceAuthToken() throws Exception {
                assert401ForExpiredServiceAuthToken(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_UNAUTHORISED)
            public void should403ForUnauthorisedServiceAuthToken() throws Exception {
                assert403ForUnauthorisedServiceAuthToken(mockMvc, happyPathRequestBuilder);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_PERMISSION_MISSING)
            public void should403ForServiceMissingS2sPermission() throws Exception {
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_CREATE_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_READ_SERVICE);
                assert403ForServiceMissingS2sPermission(mockMvc, happyPathRequestBuilder, AUTHORISED_DELETE_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfAuthServiceUnavailable() throws Exception {
                assert401IfIdamUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

            @Test
            @Override
            @DisplayName(DISPLAY_S2S_AUTH_SERVICE_UNAVAILABLE)
            public void should401IfS2sAuthServiceUnavailable() throws Exception {
                assert401IfS2sAuthServiceUnavailable(mockMvc, happyPathRequestBuilder, AUTHORISED_CRUD_SERVICE);
            }

        }

        @DisplayName("Successfully update a case payment order")
        @Test
        void shouldSuccessfullyUpdateCasePaymentOrder() throws Exception {

            // CPO-6 / AC1: Successfully allow the update of a previously created payment order in the
            //               Case Payment Order database

            // GIVEN
            CasePaymentOrderEntity originalEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                originalEntity.getId().toString(),
                uidService.generateUID(),
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isAccepted());
            verifyUpdateResponse(request, result);
            verifyDbCpoValues(request, originalEntity.getCreatedTimestamp());
            verifyDbCpoAuditValues(request, originalEntity.getCreatedTimestamp());
            verifyUpdateLogAuditValues(request, result);
        }

        @DisplayName("should fail with 400 bad request when ID is null")
        @Test
        void shouldFailWith400BadRequestWhenIdIsNull() throws Exception {

            // CPO-6 / AC2: Must return error if one or more of the mandatory parameters have not been provided (ID)

            // GIVEN
            CasePaymentOrderEntity originalEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                null,
                uidService.generateUID(),
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_REQUIRED);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(originalEntity.getId()).getRevisionType());
            verifyUpdateLogAuditValues(request, result);
        }

        @DisplayName("should fail with 400 bad request when Case ID is null")
        @Test
        void shouldFailWith400BadRequestWhenCaseIdIsNull() throws Exception {

            // CPO-6 / AC2: Must return error if one or more of the mandatory parameters have not been provided

            // GIVEN
            CasePaymentOrderEntity originalEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                originalEntity.getId().toString(),
                null,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_REQUIRED);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
            verifyUpdateLogAuditValues(request, result);
        }

        @DisplayName("should fail with 400 bad request when many fields are missing")
        @Test
        void shouldFailWith400BadRequestWhenManyFieldsAreMissing() throws Exception {

            // CPO-6 / AC2: Must return error if one or more of the mandatory parameters have not been provided

            // GIVEN
            CasePaymentOrderEntity originalEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                originalEntity.getId().toString(),
                uidService.generateUID(),
                null,
                null,
                null
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_PATH_STATUS).value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath(ERROR_PATH_ERROR).value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasSize(3)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.ORDER_REFERENCE_REQUIRED)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.ACTION_REQUIRED)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.RESPONSIBLE_PARTY_REQUIRED)));
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
            verifyUpdateLogAuditValues(request, result);
        }

        @DisplayName("should fail with 400 bad request when ID is invalid")
        @Test
        void shouldFailWith400BadRequestWhenIdIsInvalid() throws Exception {

            // CPO-6 / AC3: Must return an error if the request contains an invalid mandatory parameter

            // GIVEN
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                CPO_ID_INVALID,
                uidService.generateUID(),
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_INVALID);
            verifyUpdateLogAuditValues(request, result);
        }

        @DisplayName("should fail with 400 bad request when Case ID is invalid")
        @Test
        void shouldFailWith400BadRequestWhenCaseIdIsInvalid() throws Exception {

            // CPO-6 / AC3: Must return an error if the request contains an invalid mandatory parameter

            // GIVEN
            CasePaymentOrderEntity originalEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                originalEntity.getId().toString(),
                CASE_ID_INVALID,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_INVALID);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
            verifyUpdateLogAuditValues(request, result);
        }

        @DisplayName("should fail with 400 bad request when Order Reference is invalid")
        @Test
        void shouldFailWith400BadRequestWhenOrderReferenceIsInvalid() throws Exception {

            // CPO-6 / AC3: Must return an error if the request contains an invalid mandatory parameter

            // GIVEN
            CasePaymentOrderEntity originalEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                originalEntity.getId().toString(),
                uidService.generateUID(),
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_INVALID
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ORDER_REFERENCE_INVALID);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
            verifyUpdateLogAuditValues(request, result);
        }

        @DisplayName("should fail with 400 bad request when many fields are invalid")
        @Test
        void shouldFailWith400BadRequestWhenManyFieldsAreInvalid() throws Exception {

            // CPO-6 / AC3: Must return an error if the request contains an invalid mandatory parameter

            // GIVEN
            CasePaymentOrderEntity originalEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                originalEntity.getId().toString(),
                uidService.generateUID(),
                "",
                "",
                ""
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_PATH_STATUS).value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath(ERROR_PATH_ERROR).value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasSize(3)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.ORDER_REFERENCE_INVALID)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.ACTION_REQUIRED)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(ValidationError.RESPONSIBLE_PARTY_REQUIRED)));
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
            verifyUpdateLogAuditValues(request, result);
        }

        @DisplayName("should fail with 409 Conflict when order_reference/case_id is non-unique")
        @Test
        void shouldFailWith409ConflictWhenOrderReferencePlusCaseIdNotUnique() throws Exception {

            // CPO-6 / AC4: Must return error if order_reference/case_id is non-unique (Case order record already
            //               exists in the database for the same order reference)

            // GIVEN
            CasePaymentOrderEntity originalEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            CasePaymentOrderEntity otherEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                originalEntity.getId().toString(),
                otherEntity.getCaseId().toString(), // i.e. try to make them match
                ACTION,
                RESPONSIBLE_PARTY,
                otherEntity.getOrderReference() // i.e. try to make them match
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertHttpErrorResponse(result, HttpStatus.CONFLICT, ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
            verifyUpdateLogAuditValues(request, result);
        }

        @DisplayName("should fail with 404 Not Found when specified case payment order does not exist")
        @Test
        void shouldFailWith404NotFoundWhenCpoDoesNotExist() throws Exception {

            // CPO-6 / AC5: Must return error if the request contains a non extant record for the given ID

            // GIVEN
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                uidService.generateUID(),
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertHttpErrorResponse(result, HttpStatus.NOT_FOUND, ValidationError.CPO_NOT_FOUND);
            assertTrue(auditUtils.getAuditRevisions(request.getUUID()).isEmpty());
            verifyUpdateLogAuditValues(request, result);
        }

        private void assertBadRequestResponse(ResultActions result,
                                              String validationDetails) throws Exception {

            result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_PATH_STATUS).value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath(ERROR_PATH_ERROR).value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasSize(1)))
                .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(validationDetails)));
        }

        private void assertHttpErrorResponse(ResultActions result,
                                             HttpStatus expectedStatus,
                                             String expectedMessage) throws Exception {

            result
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath(ERROR_PATH_STATUS).value(expectedStatus.value()))
                .andExpect(jsonPath(ERROR_PATH_ERROR).value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath(ERROR_PATH_MESSAGE).value(expectedMessage));
        }

        private void verifyUpdateResponse(UpdateCasePaymentOrderRequest request,
                                          ResultActions result) throws Exception {
            result
                .andExpect(jsonPath("$.id").value(request.getId()))
                .andExpect(jsonPath("$.case_id").value(request.getCaseId()))
                .andExpect(jsonPath("$.order_reference").value(request.getOrderReference()))
                .andExpect(jsonPath("$.action").value(request.getAction()))
                .andExpect(jsonPath("$.responsible_party").value(request.getResponsibleParty()))
                .andExpect(jsonPath("$.created_by").value(IDAM_MOCK_USER_ID))
                .andExpect(jsonPath("$.created_timestamp").exists())
                .andExpect(jsonPath("$.history_exists", is(HISTORY_EXISTS_UPDATED)));
        }

        private void verifyDbCpoValues(UpdateCasePaymentOrderRequest request,
                                       LocalDateTime previousCreatedTimestamp) {
            Optional<CasePaymentOrderEntity> updatedEntity = casePaymentOrdersJpaRepository.findById(request.getUUID());

            assertTrue(updatedEntity.isPresent());
            assertEquals(request.getId(), updatedEntity.get().getId().toString());
            assertEquals(request.getCaseId(), updatedEntity.get().getCaseId().toString());
            assertEquals(request.getAction(), updatedEntity.get().getAction());
            assertEquals(request.getResponsibleParty(), updatedEntity.get().getResponsibleParty());
            assertEquals(request.getOrderReference(), updatedEntity.get().getOrderReference());
            assertEquals(IDAM_MOCK_USER_ID, updatedEntity.get().getCreatedBy());
            assertNotNull(updatedEntity.get().getCreatedTimestamp());
            assertTrue(previousCreatedTimestamp.isBefore(updatedEntity.get().getCreatedTimestamp()));
            assertEquals(HISTORY_EXISTS_UPDATED, updatedEntity.get().isHistoryExists());
        }

        private void verifyDbCpoAuditValues(UpdateCasePaymentOrderRequest request,
                                            LocalDateTime previousCreatedTimestamp) {
            // get latest
            CasePaymentOrderAuditRevision latestRevision = getLatestAuditRevision(request.getUUID());
            // expecting an update
            assertEquals(RevisionType.MOD, latestRevision.getRevisionType());
            // verify content
            assertEquals(request.getId(), latestRevision.getEntity().getId().toString());
            assertEquals(request.getCaseId(), latestRevision.getEntity().getCaseId().toString());
            assertEquals(request.getAction(), latestRevision.getEntity().getAction());
            assertEquals(request.getResponsibleParty(), latestRevision.getEntity().getResponsibleParty());
            assertEquals(request.getOrderReference(), latestRevision.getEntity().getOrderReference());
            assertEquals(IDAM_MOCK_USER_ID, latestRevision.getEntity().getCreatedBy());
            assertNotNull(latestRevision.getEntity().getCreatedTimestamp());
            assertTrue(previousCreatedTimestamp.isBefore(latestRevision.getEntity().getCreatedTimestamp()));
            assertEquals(HISTORY_EXISTS_UPDATED, latestRevision.getEntity().isHistoryExists());
        }

        private void verifyUpdateLogAuditValues(UpdateCasePaymentOrderRequest request,
                                                ResultActions result) {
            verifyLogAuditValues(result.andReturn(),
                                 AuditOperationType.UPDATE_CASE_PAYMENT_ORDER,
                                 AUTHORISED_CRUD_SERVICE,
                                 request.getId(),
                                 request.getCaseId());
        }
    }

    private CasePaymentOrderAuditRevision getLatestAuditRevision(UUID id) {

        List<CasePaymentOrderAuditRevision> auditRevisions = auditUtils.getAuditRevisions(id);

        // should not be empty as create should have added at least one.
        assertFalse(auditRevisions.isEmpty());

        return auditRevisions.get(auditRevisions.size() - 1);
    }

    private void assert404ResponseWhenSpecifiedIdentifierNotFound(HttpMethod method, String parameterName)
            throws Exception {
        // GIVEN
        List<CasePaymentOrderEntity> savedEntities =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(3);
        List<UUID> savedEntitiesUuids = savedEntities.stream()
                .map(CasePaymentOrderEntity::getId)
                .collect(Collectors.toList());
        assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = null;
        AuditOperationType auditOperationType = null;

        switch (method) {
            case GET:
                mockHttpServletRequestBuilder = get(CASE_PAYMENT_ORDERS_PATH);
                auditOperationType = AuditOperationType.GET_CASE_PAYMENT_ORDER;
                break;
            case DELETE:
                mockHttpServletRequestBuilder = delete(CASE_PAYMENT_ORDERS_PATH);
                auditOperationType = AuditOperationType.DELETE_CASE_PAYMENT_ORDER;
                break;
            default:
                fail("Invalid http method parameter supplied");
        }

        String queryParamName = null;
        String[] queryParamValue = null;
        ResultMatcher hasGeneratedLogAuditRequestMatcher = null;
        String expectedErrorMessage = null;

        switch (parameterName) {
            case IDS:
                List<UUID> nonExistentUuids = new ArrayList<>();
                nonExistentUuids.add(UUID.randomUUID());
                nonExistentUuids.add(UUID.randomUUID());
                savedEntitiesUuids.addAll(nonExistentUuids);
                String[] savedEntitiesUuidsString =
                        savedEntitiesUuids.stream().map(UUID::toString).toArray(String[]::new);
                queryParamName = IDS;
                queryParamValue = savedEntitiesUuidsString;
                hasGeneratedLogAuditRequestMatcher = hasGeneratedLogAudit(auditOperationType,
                        AUTHORISED_CRUD_SERVICE,
                        Arrays.asList(savedEntitiesUuidsString),
                        null);
                expectedErrorMessage = ValidationError.CPOS_NOT_FOUND
                        + nonExistentUuids.get(0).toString()
                        + ","
                        + nonExistentUuids.get(1).toString();
                break;
            case CASE_IDS:
                List<String> savedEntitiesCaseIds = savedEntities.stream()
                        .map(CasePaymentOrderEntity::getCaseId)
                        .map(caseId -> Long.toString(caseId))
                        .collect(Collectors.toList());

                List<String> nonExistentCpoIdentifiers = new ArrayList<>();
                nonExistentCpoIdentifiers.add(uidService.generateUID());
                nonExistentCpoIdentifiers.add(uidService.generateUID());
                savedEntitiesCaseIds.addAll(nonExistentCpoIdentifiers);
                queryParamName = CASE_IDS;
                queryParamValue = savedEntitiesCaseIds.toArray(String[]::new);
                hasGeneratedLogAuditRequestMatcher = hasGeneratedLogAudit(auditOperationType,
                        AUTHORISED_CRUD_SERVICE,
                        null,
                        savedEntitiesCaseIds);
                expectedErrorMessage = ValidationError.CPOS_NOT_FOUND
                        + nonExistentCpoIdentifiers.get(0)
                        + ","
                        + nonExistentCpoIdentifiers.get(1);
                break;
            default:
                fail("Invalid query parameter name supplied");

        }

        mockMvc.perform(
                mockHttpServletRequestBuilder
                        .headers(createHttpHeaders(AUTHORISED_CRUD_SERVICE))
                        .queryParam(queryParamName, queryParamValue))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(ERROR_PATH_MESSAGE,
                        is(expectedErrorMessage)))
                .andExpect(hasGeneratedLogAuditRequestMatcher);

        if (method.equals(HttpMethod.DELETE)) {
            assertEquals(savedEntities.size(), casePaymentOrdersJpaRepository.findAllById(savedEntitiesUuids).size());
        }
    }

}
