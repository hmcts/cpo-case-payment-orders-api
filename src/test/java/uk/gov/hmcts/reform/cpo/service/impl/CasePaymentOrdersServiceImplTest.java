package uk.gov.hmcts.reform.cpo.service.impl;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CaseIdOrderReferenceUniqueConstraintException;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.exception.IdAMIdCannotBeRetrievedException;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapperImpl;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity.UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT;
import static uk.gov.hmcts.reform.cpo.validators.ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE;
import static uk.gov.hmcts.reform.cpo.validators.ValidationError.IDAM_ID_RETRIEVE_ERROR;

class CasePaymentOrdersServiceImplTest implements BaseTest {

    @InjectMocks
    private CasePaymentOrdersServiceImpl casePaymentOrdersService;

    @Mock
    private CasePaymentOrdersRepository casePaymentOrdersRepository;

    @Mock
    private CasePaymentOrderMapperImpl mapper;

    @Mock
    private SecurityUtils securityUtils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Create Case Payment Order")
    class CreateCasePaymentOrder {

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequest;

        private CasePaymentOrder casePaymentOrderIncoming;

        private CasePaymentOrderEntity requestEntity;

        private CasePaymentOrderEntity savedEntity;

        @BeforeEach
        public void setUp() {

            setupSecurityUtilsMock();

            // create entity and domain model from same sample data in `BaseTest`
            createCasePaymentOrderRequest = createCreateCasePaymentOrderRequest();
            casePaymentOrderIncoming = createCasePaymentOrder();
            requestEntity = createCasePaymentOrderEntity();
            savedEntity = createCasePaymentOrderEntity();
        }

        @Test
        @DisplayName("Should create CasePaymentOrder successfully")
        void shouldCreateCasePaymentOrder() {
            given(mapper.toEntity(createCasePaymentOrderRequest, CREATED_BY)).willReturn(requestEntity);
            given(casePaymentOrdersRepository.saveAndFlush(requestEntity)).willReturn(savedEntity);
            given(mapper.toDomainModel(savedEntity)).willReturn(casePaymentOrderIncoming);
            CasePaymentOrder caseOrderReturn = casePaymentOrdersService
                .createCasePaymentOrder(createCasePaymentOrderRequest);
            assertThat("UUID does not match expected", caseOrderReturn.getId().toString().equals(CPO_ID_VALID_1));
            assertThat("Returned entity does not match expected", caseOrderReturn.equals(casePaymentOrderIncoming));
        }

        @Test
        @DisplayName("Should throw error when request has non-unique order reference and case id pairing")
        void shouldErrorWhenNonUniquePairing() {
            given(mapper.toEntity(createCasePaymentOrderRequest, CREATED_BY)).willReturn(requestEntity);
            ConstraintViolationException exception =
                new ConstraintViolationException(
                    "",
                    null,
                    UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT
                );
            given(casePaymentOrdersRepository.saveAndFlush(requestEntity)).willThrow(
                new DataIntegrityViolationException("", exception));
            assertThatThrownBy(() -> casePaymentOrdersService.createCasePaymentOrder(createCasePaymentOrderRequest))
                .isInstanceOf(CaseIdOrderReferenceUniqueConstraintException.class)
                .hasMessageContaining(CASE_ID_ORDER_REFERENCE_UNIQUE);
        }

        @Test
        @DisplayName("Should throw error when IdAM Id cannot be retrieved")
        void shouldErrorWhenCannotRetrieveIdamId() {
            given(securityUtils.getUserInfo()).willThrow(new RuntimeException());
            assertThatThrownBy(() -> casePaymentOrdersService.createCasePaymentOrder(createCasePaymentOrderRequest))
                .isInstanceOf(IdAMIdCannotBeRetrievedException.class)
                .hasMessageContaining(IDAM_ID_RETRIEVE_ERROR);
        }
    }

    @Nested
    @DisplayName("Update Case Payment Order")
    class UpdateCasePaymentOrder {

        private UpdateCasePaymentOrderRequest updateCasePaymentOrderRequest;

        private CasePaymentOrderEntity casePaymentOrderEntity;

        private CasePaymentOrderEntity savedEntity;

        private CasePaymentOrder casePaymentOrderResponse;

        @BeforeEach
        public void setUp() {

            setupSecurityUtilsMock();

            // create entity and domain model from same sample data in `BaseTest`
            // :: update request
            updateCasePaymentOrderRequest = createUpdateCasePaymentOrderRequest();
            // :: loaded entity
            casePaymentOrderEntity = createCasePaymentOrderEntity();
            // :: saved entity
            savedEntity = createCasePaymentOrderEntity();
            // :: response model
            casePaymentOrderResponse = createCasePaymentOrder();
        }

        @Test
        @DisplayName("Should update CasePaymentOrder successfully")
        void shouldUpdateCasePaymentOrder() {

            // GIVEN
            // :: the load
            UUID id = updateCasePaymentOrderRequest.getUUID();
            given(casePaymentOrdersRepository.findById(id)).willReturn(Optional.of(casePaymentOrderEntity));
            // :: the save
            given(casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity)).willReturn(savedEntity);
            // :: the conversion
            given(mapper.toDomainModel(savedEntity)).willReturn(casePaymentOrderResponse);

            // WHEN
            CasePaymentOrder response = casePaymentOrdersService.updateCasePaymentOrder(updateCasePaymentOrderRequest);

            // THEN
            verify(casePaymentOrdersRepository, times(1)).saveAndFlush(casePaymentOrderEntity);
            assertThat("UUID should match expected", response.getId().equals(id));
            assertThat("Returned model should match expected", response.equals(casePaymentOrderResponse));
        }

        @Test
        @DisplayName("Should throw CasePaymentOrder could not be found error when CPO is not found")
        void shouldThrowCpoCouldNotBeFoundExceptionWhenCpoNotFound() {

            // GIVEN
            UUID id = updateCasePaymentOrderRequest.getUUID();
            given(casePaymentOrdersRepository.findById(id)).willReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> casePaymentOrdersService.updateCasePaymentOrder(updateCasePaymentOrderRequest))
                .isInstanceOf(CasePaymentOrderCouldNotBeFoundException.class)
                .hasMessageContaining(ValidationError.CPO_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw CaseIdOrderReferenceUniqueConstraintException when correct constraint encountered")
        void shouldThrowCaseIdOrderReferenceUniqueConstraintExceptionWhenCorrectConstraintTriggered() {

            // GIVEN
            // :: the load
            UUID id = updateCasePaymentOrderRequest.getUUID();
            given(casePaymentOrdersRepository.findById(id)).willReturn(Optional.of(casePaymentOrderEntity));
            // :: the save
            ConstraintViolationException constraintViolationException =
                new ConstraintViolationException("",
                                                 null,
                                                 UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT);
            given(casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity))
                .willThrow(new DataIntegrityViolationException("", constraintViolationException));

            // WHEN / THEN
            assertThatThrownBy(() -> casePaymentOrdersService.updateCasePaymentOrder(updateCasePaymentOrderRequest))
                .isInstanceOf(CaseIdOrderReferenceUniqueConstraintException.class)
                .hasMessageContaining(ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE);
        }

        @Test
        @DisplayName("Should re-throw DataIntegrityViolationException when incorrect constraint encountered")
        void shouldRethrowDataIntegrityViolationExceptionWhenIncorrectConstraintTriggered() {

            // GIVEN
            // :: the load
            UUID id = updateCasePaymentOrderRequest.getUUID();
            given(casePaymentOrdersRepository.findById(id)).willReturn(Optional.of(casePaymentOrderEntity));
            // :: the save
            ConstraintViolationException constraintViolationException =
                new ConstraintViolationException("",
                                                 null,
                                                 "DIFFERENT-CONSTRAINT-NAME");
            given(casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity))
                .willThrow(new DataIntegrityViolationException("", constraintViolationException));

            // WHEN / THEN
            assertThatThrownBy(() -> casePaymentOrdersService.updateCasePaymentOrder(updateCasePaymentOrderRequest))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Should re-throw DataIntegrityViolationException when no constraint encountered")
        void shouldRethrowDataIntegrityViolationExceptionWhenNoConstraintTriggered() {

            // GIVEN
            // :: the load
            UUID id = updateCasePaymentOrderRequest.getUUID();
            given(casePaymentOrdersRepository.findById(id)).willReturn(Optional.of(casePaymentOrderEntity));
            // :: the save
            given(casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity))
                .willThrow(new DataIntegrityViolationException(""));

            // WHEN / THEN
            assertThatThrownBy(() -> casePaymentOrdersService.updateCasePaymentOrder(updateCasePaymentOrderRequest))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Should throw error when IdAM Id cannot be retrieved")
        void shouldErrorWhenCannotRetrieveIdamId() {
            // GIVEN
            given(securityUtils.getUserInfo()).willThrow(new RuntimeException());

            // WHEN / THEN
            assertThatThrownBy(() -> casePaymentOrdersService.updateCasePaymentOrder(updateCasePaymentOrderRequest))
                .isInstanceOf(IdAMIdCannotBeRetrievedException.class)
                .hasMessageContaining(IDAM_ID_RETRIEVE_ERROR);
        }

    }

    private void setupSecurityUtilsMock() {
        UserInfo userInfo = UserInfo.builder()
            .uid(CREATED_BY)
            .build();

        given(securityUtils.getUserInfo()).willReturn(userInfo);
    }

}
