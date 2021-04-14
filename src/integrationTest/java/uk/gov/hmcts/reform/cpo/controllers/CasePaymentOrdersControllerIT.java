package uk.gov.hmcts.reform.cpo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrderAuditRevision;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderAuditUtils;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;
import uk.gov.hmcts.reform.cpo.utils.UIDService;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;

class CasePaymentOrdersControllerIT extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CasePaymentOrdersRepository repository;

    @Autowired
    private CasePaymentOrderAuditUtils auditUtils;

    @Autowired
    private CasePaymentOrderEntityGenerator casePaymentOrderEntityGenerator;

    @Autowired
    private UIDService uidService;

    @BeforeEach
    public void setUp() {
        repository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("PUT /case-payment-orders")
    class UpdateCasePaymentOrder {

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
                EFFECTIVE_FROM,
                uidService.generateUID(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isAccepted());
            verifyUpdateResponse(request, result);
            verifyDbCpoValues(request, originalEntity.getCreatedTimestamp());
            verifyDbCpoAuditValues(request, originalEntity.getCreatedTimestamp());
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
                EFFECTIVE_FROM,
                uidService.generateUID(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_REQUIRED);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(originalEntity.getId()).getRevisionType());
        }

        @DisplayName("should fail with 400 bad request when Effective From is null")
        @Test
        void shouldFailWith400BadRequestWhenEffectiveFromIsNull() throws Exception {

            // CPO-6 / AC2: Must return error if one or more of the mandatory parameters have not been provided

            // GIVEN
            CasePaymentOrderEntity originalEntity =
                casePaymentOrderEntityGenerator.generateAndSaveEntities(1).get(0);
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                originalEntity.getId().toString(),
                null,
                uidService.generateUID(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.EFFECTIVE_FROM_REQUIRED);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
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
                EFFECTIVE_FROM,
                null,
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_REQUIRED);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
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
                EFFECTIVE_FROM,
                uidService.generateUID(),
                null,
                null,
                null
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
        }

        @DisplayName("should fail with 400 bad request when ID is invalid")
        @Test
        void shouldFailWith400BadRequestWhenIdIsInvalid() throws Exception {

            // CPO-6 / AC3: Must return an error if the request contains an invalid mandatory parameter

            // GIVEN
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                CPO_ID_INVALID,
                EFFECTIVE_FROM,
                uidService.generateUID(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_INVALID);
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
                EFFECTIVE_FROM,
                CASE_ID_INVALID,
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_INVALID);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
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
                EFFECTIVE_FROM,
                uidService.generateUID(),
                "",
                "",
                ""
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
                EFFECTIVE_FROM,
                otherEntity.getCaseId().toString(), // i.e. try to make them match
                otherEntity.getOrderReference(), // i.e. try to make them match
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertHttpErrorResponse(result, HttpStatus.CONFLICT, ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE);
            // check latest audit does not show update (i.e. modified)
            assertNotSame(RevisionType.MOD, getLatestAuditRevision(request.getUUID()).getRevisionType());
        }

        @DisplayName("should fail with 404 Not Found when specified case payment order does not exist")
        @Test
        void shouldFailWith404NotFoundWhenCpoDoesNotExist() throws Exception {

            // CPO-6 / AC5: Must return error if the request contains a non extant record for the given ID

            // GIVEN
            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                uidService.generateUID(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertHttpErrorResponse(result, HttpStatus.NOT_FOUND, ValidationError.CPO_NOT_FOUND);
            assertTrue(auditUtils.getAuditRevisions(request.getUUID()).isEmpty());
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
                .andExpect(jsonPath("$.effective_from").value(request.getEffectiveFrom().format(formatter)))
                .andExpect(jsonPath("$.case_id").value(request.getCaseId()))
                .andExpect(jsonPath("$.order_reference").value(request.getOrderReference()))
                .andExpect(jsonPath("$.action").value(request.getAction()))
                .andExpect(jsonPath("$.responsible_party").value(request.getResponsibleParty()))
                .andExpect(jsonPath("$.created_by").value(CREATED_BY_IDAM_MOCK))
                .andExpect(jsonPath("$.created_timestamp").exists());
        }

        private void verifyDbCpoValues(UpdateCasePaymentOrderRequest request,
                                       LocalDateTime previousCreatedTimestamp) {
            Optional<CasePaymentOrderEntity> updatedEntity = repository.findById(request.getUUID());

            if (updatedEntity.isPresent()) {
                assertEquals(request.getId(), updatedEntity.get().getId().toString());
                assertEquals(request.getEffectiveFrom(), updatedEntity.get().getEffectiveFrom());
                assertEquals(request.getCaseId(), updatedEntity.get().getCaseId().toString());
                assertEquals(request.getAction(), updatedEntity.get().getAction());
                assertEquals(request.getResponsibleParty(), updatedEntity.get().getResponsibleParty());
                assertEquals(request.getOrderReference(), updatedEntity.get().getOrderReference());
                assertEquals(CREATED_BY_IDAM_MOCK, updatedEntity.get().getCreatedBy());
                assertTrue(previousCreatedTimestamp.isBefore(updatedEntity.get().getCreatedTimestamp()));
            } else {
                fail("Saved entity not found");
            }
        }

        private void verifyDbCpoAuditValues(UpdateCasePaymentOrderRequest request,
                                            LocalDateTime previousCreatedTimestamp) {
            // get latest
            CasePaymentOrderAuditRevision latestRevision = getLatestAuditRevision(request.getUUID());
            // expecting an update
            assertEquals(RevisionType.MOD, latestRevision.getRevisionType());
            // verify content
            assertEquals(request.getId(), latestRevision.getEntity().getId().toString());
            assertEquals(request.getEffectiveFrom(), latestRevision.getEntity().getEffectiveFrom());
            assertEquals(request.getCaseId(), latestRevision.getEntity().getCaseId().toString());
            assertEquals(request.getAction(), latestRevision.getEntity().getAction());
            assertEquals(request.getResponsibleParty(), latestRevision.getEntity().getResponsibleParty());
            assertEquals(request.getOrderReference(), latestRevision.getEntity().getOrderReference());
            assertEquals(CREATED_BY_IDAM_MOCK, latestRevision.getEntity().getCreatedBy());
            assertNotNull(latestRevision.getEntity().getCreatedTimestamp());
            assertTrue(previousCreatedTimestamp.isBefore(latestRevision.getEntity().getCreatedTimestamp()));
        }

        private CasePaymentOrderAuditRevision getLatestAuditRevision(UUID id) {

            List<CasePaymentOrderAuditRevision> auditRevisions = auditUtils.getAuditRevisions(id);

            // should not be empty as create should have added at least one.
            assertFalse(auditRevisions.isEmpty());

            return auditRevisions.get(auditRevisions.size() - 1);
        }

    }

}
