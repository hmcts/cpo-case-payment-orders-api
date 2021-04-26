package uk.gov.hmcts.reform.cpo.service.impl;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CaseIdOrderReferenceUniqueConstraintException;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.exception.IdAMIdCannotBeRetrievedException;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapperImpl;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;
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

    @Captor
    ArgumentCaptor<List<UUID>> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<List<Long>> caseIdsArgumentCaptor;

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
            assertEquals(HISTORY_EXISTS_DEFAULT, caseOrderReturn.isHistoryExists());
        }

        @Test
        @DisplayName("Should successfully set fields when creating CasePaymentOrder")
        void shouldSetFieldsWhenCreatingCasePayment() {
            given(mapper.toEntity(createCasePaymentOrderRequest, CREATED_BY)).willReturn(requestEntity);
            given(casePaymentOrdersRepository.saveAndFlush(requestEntity)).willReturn(savedEntity);
            given(mapper.toDomainModel(savedEntity)).willReturn(casePaymentOrderIncoming);

            CasePaymentOrder caseOrderReturn = casePaymentOrdersService
                .createCasePaymentOrder(createCasePaymentOrderRequest);

            // verify service call
            ArgumentCaptor<CasePaymentOrderEntity> captor =
                ArgumentCaptor.forClass(CasePaymentOrderEntity.class);

            // THEN
            verify(casePaymentOrdersRepository, times(1)).saveAndFlush(captor.capture());
            assertEquals(HISTORY_EXISTS_DEFAULT, caseOrderReturn.isHistoryExists());
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
    @DisplayName("Delete Case Payment Order")
    class DeleteCasePaymentOrder {

        private final List<UUID> uuidsToDelete = List.of(UUID.randomUUID(), UUID.randomUUID());

        private final List<Long> caseIdsToDelete = List.of(123L, 456L);

        private CasePaymentOrderQueryFilter uuidFilter;

        private CasePaymentOrderQueryFilter caseIdFilter;

        @BeforeEach
        void beforeEachTest() {
            uuidFilter = CasePaymentOrderQueryFilter.builder()
                .cpoIds(uuidsToDelete.stream()
                            .map(UUID::toString)
                            .collect(Collectors.toList()))
                .caseIds(Collections.emptyList())
                .build();
            caseIdFilter = CasePaymentOrderQueryFilter.builder()
                .cpoIds(Collections.emptyList())
                .caseIds(caseIdsToDelete.stream()
                             .map(Object::toString)
                             .collect(Collectors.toList()))
                .build();
        }

        @Test
        void deleteCasePaymentOrdersById() {
            casePaymentOrdersService.deleteCasePaymentOrders(uuidFilter);

            verify(casePaymentOrdersRepository).deleteByUuids(uuidArgumentCaptor.capture());
            assertEquals(uuidsToDelete, uuidArgumentCaptor.getValue());

            verify(casePaymentOrdersRepository).deleteAuditEntriesByUuids(uuidArgumentCaptor.capture());
            assertEquals(uuidsToDelete, uuidArgumentCaptor.getValue());
        }

        @Test
        void deleteCasePaymentOrdersByCaseIds() {
            casePaymentOrdersService.deleteCasePaymentOrders(caseIdFilter);

            verify(casePaymentOrdersRepository).deleteByCaseIds(caseIdsArgumentCaptor.capture());
            assertEquals(caseIdsArgumentCaptor.getValue(), caseIdsToDelete);

            verify(casePaymentOrdersRepository).deleteAuditEntriesByCaseIds(caseIdsArgumentCaptor.capture());
            assertEquals(caseIdsArgumentCaptor.getValue(), caseIdsToDelete);
        }
    }


    @Nested
    @DisplayName("Get Case Payment Order")
    class GetCasePaymentOrder {

        private final List<String> casesIds = List.of("1609243447569251", "1609243447569252", "1609243447569253");

        private final List<String> ids = List.of("df54651b-3227-4067-9f23-6ffb32e2c6bd",
                                                 "d702ef36-0ca7-46e9-8a00-ef044d78453e",
                                                 "d702ef36-0ca7-46e9-8a00-ef044d78453e");

        @Test
        void passForListIds() {
            final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
                .cpoIds(ids)
                .caseIds(Collections.emptyList())
                .pageable(getPageRequest())
                .build();

            when(casePaymentOrdersRepository.findByIdIn(anyList(), ArgumentMatchers.<Pageable>any())).thenReturn(
                getEntityPages());

            final Page<CasePaymentOrder> pages = casePaymentOrdersService.getCasePaymentOrders(
                casePaymentOrderQueryFilter);

            assertTrue("The getNumberOfElements should be 0.", pages.getNumberOfElements() == 3);
        }

        @Test
        void passForListCasesIds() {
            final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
                .cpoIds(Collections.emptyList())
                .caseIds(casesIds)
                .pageable(getPageRequest())
                .build();

            when(casePaymentOrdersRepository.findByCaseIdIn(anyList(), ArgumentMatchers.<Pageable>any())).thenReturn(
                getEntityPages());

            final Page<CasePaymentOrder> pages = casePaymentOrdersService.getCasePaymentOrders(
                casePaymentOrderQueryFilter);

            assertTrue("The getNumberOfElements should be 0.", pages.getNumberOfElements() == 3);
        }

        @Test
        void failForListCasesIds() {
            final ArrayList<CasePaymentOrderEntity> casePaymentOrders = new ArrayList<>();
            final Page<CasePaymentOrderEntity> pageImpl = new PageImpl<>(
                casePaymentOrders,
                getPageRequest(),
                3
            );
            final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
                .cpoIds(Collections.emptyList())
                .caseIds(casesIds)
                .pageable(getPageRequest())
                .build();

            when(casePaymentOrdersRepository.findByCaseIdIn(anyList(), any())).thenReturn(
                pageImpl);

            assertThatThrownBy(() -> casePaymentOrdersService.getCasePaymentOrders(casePaymentOrderQueryFilter))
                .isInstanceOf(CasePaymentOrderCouldNotBeFoundException.class)
                .hasMessageContaining(ValidationError.CPO_NOT_FOUND);
        }

        private Page<CasePaymentOrderEntity> getEntityPages() {
            final PageRequest pageRequest = getPageRequest();
            return new PageImpl<CasePaymentOrderEntity>(createListOfCasePaymentOrderEntity(), pageRequest, 3);
        }


        private List<CasePaymentOrderEntity> createListOfCasePaymentOrderEntity() {
            final ArrayList<CasePaymentOrderEntity> casePaymentOrders = new ArrayList<>();

            final CasePaymentOrderEntity casePaymentOrderEntity = new CasePaymentOrderEntity();
            casePaymentOrderEntity.setAction("action");
            casePaymentOrderEntity.setCaseId(Long.parseLong("1609243447569251"));
            casePaymentOrderEntity.setCreatedBy("action1");
            casePaymentOrderEntity.setOrderReference("action1");
            casePaymentOrderEntity.setEffectiveFrom(LocalDateTime.now());
            casePaymentOrderEntity.setCreatedTimestamp(LocalDateTime.now());
            casePaymentOrderEntity.setResponsibleParty("setResponsibleParty");
            casePaymentOrders.add(casePaymentOrderEntity);

            final CasePaymentOrderEntity casePaymentOrderEntity1 = new CasePaymentOrderEntity();
            casePaymentOrderEntity1.setAction("action");
            casePaymentOrderEntity1.setCaseId(Long.parseLong("1609243447569252"));
            casePaymentOrderEntity1.setCreatedBy("action1");
            casePaymentOrderEntity1.setOrderReference("Baction2");
            casePaymentOrderEntity1.setEffectiveFrom(LocalDateTime.now());
            casePaymentOrderEntity1.setCreatedTimestamp(LocalDateTime.now());
            casePaymentOrderEntity1.setResponsibleParty("setResponsibleParty");
            casePaymentOrders.add(casePaymentOrderEntity1);

            final CasePaymentOrderEntity casePaymentOrderEntity2 = new CasePaymentOrderEntity();
            casePaymentOrderEntity2.setAction("action");
            casePaymentOrderEntity2.setCaseId(Long.parseLong("1609243447569253"));
            casePaymentOrderEntity2.setCreatedBy("action1");
            casePaymentOrderEntity2.setOrderReference("Caction3");
            casePaymentOrderEntity2.setEffectiveFrom(LocalDateTime.now());
            casePaymentOrderEntity2.setCreatedTimestamp(LocalDateTime.now());
            casePaymentOrderEntity2.setResponsibleParty("setResponsibleParty");
            casePaymentOrders.add(casePaymentOrderEntity2);
            return casePaymentOrders;
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
            assertEquals(HISTORY_EXISTS_UPDATED, casePaymentOrderEntity.isHistoryExists());
        }

        @Test
        @DisplayName("Should successfully set fields when updating CasePaymentOrder")
        void shouldSetFieldsWhenUpdatingCasePayment() {

            LocalDateTime previousCreateDateTime = casePaymentOrderEntity.getCreatedTimestamp();

            // GIVEN
            // :: the load
            UUID id = updateCasePaymentOrderRequest.getUUID();
            given(casePaymentOrdersRepository.findById(id)).willReturn(Optional.of(casePaymentOrderEntity));
            // :: the save
            given(casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity)).willReturn(savedEntity);
            // :: the conversion
            given(mapper.toDomainModel(savedEntity)).willReturn(casePaymentOrderResponse);

            // WHEN
            casePaymentOrdersService.updateCasePaymentOrder(updateCasePaymentOrderRequest);

            // verify service call
            ArgumentCaptor<CasePaymentOrderEntity> captor =
                ArgumentCaptor.forClass(CasePaymentOrderEntity.class);

            // THEN
            verify(casePaymentOrdersRepository, times(1)).saveAndFlush(captor.capture());

            assertTrue("", previousCreateDateTime.isBefore(captor.getValue().getCreatedTimestamp()));
            assertEquals(captor.getValue().isHistoryExists(), HISTORY_EXISTS_UPDATED);
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
