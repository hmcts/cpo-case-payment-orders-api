package uk.gov.hmcts.reform.cpo.service;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CaseIdOrderReferenceUniqueConstraintException;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity.UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT;

@ExtendWith(MockitoExtension.class)
class CasePaymentOrdersServiceImplTest {

    @InjectMocks
    private CasePaymentOrdersServiceImpl casePaymentOrdersService;

    @Mock
    private CasePaymentOrdersRepository casePaymentOrdersRepository;

    @Mock
    private CasePaymentOrderMapper mapper;

    @Mock
    private SecurityUtils securityUtils;

    private static final LocalDateTime EFFECTIVE_FROM = LocalDateTime.of(2021, Month.MARCH, 24, 11, 48,
                                                           32);
    private static final Long CASE_ID = 1_122_334_455_667_788L;
    private static final String ACTION = "action";
    private static final String RESPONSIBLE_PARTY = "responsibleParty";
    private static final String ORDER_REFERENCE = "orderReference";
    private static final UUID ID = UUID.randomUUID();
    private static final String CREATED_BY = "createdBy";
    private static final LocalDateTime CREATED_TIMESTAMP = LocalDateTime.now();

    @BeforeEach
    public void setUp() {
        UserInfo userInfo = UserInfo.builder()
            .uid(CREATED_BY)
            .build();

        given(securityUtils.getUserInfo()).willReturn(userInfo);
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

            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // loaded entity
            casePaymentOrderEntity = new CasePaymentOrderEntity();
            casePaymentOrderEntity.setEffectiveFrom(EFFECTIVE_FROM);
            casePaymentOrderEntity.setCaseId(CASE_ID);
            casePaymentOrderEntity.setAction(ACTION);
            casePaymentOrderEntity.setResponsibleParty(RESPONSIBLE_PARTY);
            casePaymentOrderEntity.setOrderReference(ORDER_REFERENCE);
            casePaymentOrderEntity.setCreatedBy(CREATED_BY);

            // saved entity
            savedEntity = new CasePaymentOrderEntity();
            savedEntity.setEffectiveFrom(EFFECTIVE_FROM);
            savedEntity.setCaseId(CASE_ID);
            savedEntity.setAction(ACTION);
            savedEntity.setResponsibleParty(RESPONSIBLE_PARTY);
            savedEntity.setOrderReference(ORDER_REFERENCE);
            savedEntity.setCreatedBy(CREATED_BY);
            savedEntity.setCreatedTimestamp(CREATED_TIMESTAMP);

            // response model
            casePaymentOrderResponse = CasePaymentOrder.builder()
                .effectiveFrom(EFFECTIVE_FROM)
                .caseId(CASE_ID)
                .action(ACTION)
                .responsibleParty(RESPONSIBLE_PARTY)
                .orderReference(ORDER_REFERENCE)
                .createdBy(CREATED_BY)
                .id(ID)
                .createdTimestamp(CREATED_TIMESTAMP)
                .build();
        }

        @Test
        @DisplayName("Should update CasePaymentOrder successfully")
        void shouldUpdateCasePaymentOrder() {

            // GIVEN
            // :: the load
            given(casePaymentOrdersRepository.findById(ID)).willReturn(Optional.of(casePaymentOrderEntity));
            // :: the save
            given(casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity)).willReturn(savedEntity);
            // :: the conversion
            given(mapper.toDomainModel(savedEntity)).willReturn(casePaymentOrderResponse);

            // WHEN
            CasePaymentOrder response = casePaymentOrdersService.updateCasePaymentOrder(updateCasePaymentOrderRequest);

            // THEN
            verify(casePaymentOrdersRepository, times(1)).saveAndFlush(casePaymentOrderEntity);
            assertThat("UUID does not match expected", response.getId().equals(ID));
            assertThat("Returned model does not match expected", response.equals(casePaymentOrderResponse));
        }

        @Test
        @DisplayName("Should throw CasePaymentOrder could not be found error when CPO is not found")
        void shouldThrowCpoCouldNotBeFoundExceptionWhenCpoNotFound() {

            // GIVEN
            given(casePaymentOrdersRepository.findById(ID)).willReturn(Optional.empty());

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
            given(casePaymentOrdersRepository.findById(ID)).willReturn(Optional.of(casePaymentOrderEntity));
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
            given(casePaymentOrdersRepository.findById(ID)).willReturn(Optional.of(casePaymentOrderEntity));
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
            given(casePaymentOrdersRepository.findById(ID)).willReturn(Optional.of(casePaymentOrderEntity));
            // :: the save
            given(casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity))
                .willThrow(new DataIntegrityViolationException(""));

            // WHEN / THEN
            assertThatThrownBy(() -> casePaymentOrdersService.updateCasePaymentOrder(updateCasePaymentOrderRequest))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

    }

}
