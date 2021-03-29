package uk.gov.hmcts.reform.cpo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapperImpl;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

class CasePaymentOrdersServiceImplTest {

    @InjectMocks
    private CasePaymentOrdersServiceImpl casePaymentOrdersService;

    @Mock
    private CasePaymentOrdersRepository casePaymentOrdersRepository;

    @Mock
    private CasePaymentOrderMapperImpl mapper;

    @Mock
    private CasePaymentOrder casePaymentOrderIncoming;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private CreateCasePaymentOrderRequest createCasePaymentOrderRequest;

    @Mock
    private UserInfo userInfo;

    @Mock
    CasePaymentOrderEntity requestEntity;

    @Mock
    CasePaymentOrderEntity savedEntity;

    private static final LocalDateTime EFFECTIVE_FROM = LocalDateTime.of(2021, Month.MARCH, 24, 11, 48,
                                                           32);
    private static final Long CASE_ID = 1122334455667788L;
    private static final String CASE_TYPE_ID = "caseType";
    private static final String ACTION = "action";
    private static final String RESPONSIBLE_PARTY = "responsibleParty";
    private static final String ORDER_REFERENCE = "orderReference";
    private static final UUID ID = UUID.randomUUID();
    private static final String CREATED_BY = "createdBy";
    private static final LocalDateTime CREATED_TIMESTAMP = LocalDateTime.now();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Create Case Payment Order")
    class CreateCasePaymentOrder {

        @BeforeEach
        public void setUp() {


            userInfo = UserInfo.builder()
                .uid(CREATED_BY)
                .build();

            casePaymentOrdersService = new CasePaymentOrdersServiceImpl(casePaymentOrdersRepository, mapper,
                                                                        securityUtils);
            createCasePaymentOrderRequest = new CreateCasePaymentOrderRequest(
                EFFECTIVE_FROM,
                CASE_ID,
                CASE_TYPE_ID,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE
            );

            casePaymentOrderIncoming = CasePaymentOrder.builder()
                .effectiveFrom(EFFECTIVE_FROM)
                .caseId(CASE_ID)
                .caseTypeId(CASE_TYPE_ID)
                .action(ACTION)
                .responsibleParty(RESPONSIBLE_PARTY)
                .orderReference(ORDER_REFERENCE)
                .createdBy(CREATED_BY)
                .id(ID)
                .createdTimestamp(CREATED_TIMESTAMP)
                .build();

            requestEntity.setEffectiveFrom(EFFECTIVE_FROM);
            requestEntity.setCaseId(CASE_ID);
            requestEntity.setCaseTypeId(CASE_TYPE_ID);
            requestEntity.setAction(ACTION);
            requestEntity.setResponsibleParty(RESPONSIBLE_PARTY);
            requestEntity.setOrderReference(ORDER_REFERENCE);
            requestEntity.setCreatedBy(CREATED_BY);

            savedEntity.setEffectiveFrom(EFFECTIVE_FROM);
            savedEntity.setCaseId(CASE_ID);
            savedEntity.setCaseTypeId(CASE_TYPE_ID);
            savedEntity.setAction(ACTION);
            savedEntity.setResponsibleParty(RESPONSIBLE_PARTY);
            savedEntity.setOrderReference(ORDER_REFERENCE);
            savedEntity.setCreatedBy(CREATED_BY);
            savedEntity.setCreatedTimestamp(CREATED_TIMESTAMP);
        }

        @Test
        @DisplayName("Should create CasePaymentOrder successfully")
        void shouldCreateCasePaymentOrder() {
            given(securityUtils.getUserInfo()).willReturn(userInfo);
            given(mapper.toEntity(createCasePaymentOrderRequest, CREATED_BY)).willReturn(requestEntity);
            given(casePaymentOrdersRepository.saveAndFlush(requestEntity)).willReturn(savedEntity);
            given(mapper.toDomainModel(savedEntity)).willReturn(casePaymentOrderIncoming);
            CasePaymentOrder caseOrderReturn = casePaymentOrdersService
                .createCasePaymentOrder(createCasePaymentOrderRequest);
            assertThat("UUID does not match expected", caseOrderReturn.getId().equals(ID));
            assertThat("Returned entity does not match expected", caseOrderReturn.equals(casePaymentOrderIncoming));
        }
    }
}
