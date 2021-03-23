package uk.gov.hmcts.reform.cpo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.IdamRepository;
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
    private IdamRepository idamRepository;

    @Mock
    private CreateCasePaymentOrderRequest createCasePaymentOrderRequest;

    @Mock
    private UserInfo userInfo;

    private LocalDateTime effectiveFrom = LocalDateTime.of(2021, Month.MARCH, 24, 11, 48,
                                                            32);
    private Long caseId = 1122334455667788L;
    private String caseTypeId = "caseType";
    private String action = "action";
    private String responsibleParty = "responsibleParty";
    private String orderReference = "orderReference";
    private UUID id = UUID.randomUUID();
    private String createdBy = "createdBy";
    private LocalDateTime date = LocalDateTime.now();
    private String userToken = "userToken";

    @BeforeEach
    public void setUp() {


        MockitoAnnotations.openMocks(this);

        userInfo = UserInfo.builder()
            .uid(createdBy)
            .build();

        casePaymentOrdersService = new CasePaymentOrdersServiceImpl(casePaymentOrdersRepository, mapper,
                                                                    idamRepository);
        createCasePaymentOrderRequest = new CreateCasePaymentOrderRequest(
            effectiveFrom,
            caseId,
            caseTypeId,
            action,
            responsibleParty,
            orderReference
        );

        casePaymentOrderIncoming = CasePaymentOrder.builder()
            .effectiveFrom(effectiveFrom)
            .caseId(caseId)
            .caseTypeId(caseTypeId)
            .action(action)
            .responsibleParty(responsibleParty)
            .orderReference(orderReference)
            .createdBy(createdBy)
            .id(id)
            .createdTimestamp(date)
            .build();
    }

    @Nested
    @DisplayName("Create Case Payment Order")
    class CreateCasePaymentOrder {

        @Test
        @DisplayName("Should create CasePaymentOrder successfully")
        void shouldCreateCasePaymentOrder() {
            given(idamRepository.getUserInfo(userToken)).willReturn(userInfo);
            given(casePaymentOrdersService.createCasePaymentOrder(createCasePaymentOrderRequest,userToken))
                .willReturn(casePaymentOrderIncoming);
            CasePaymentOrder caseOrderReturn = casePaymentOrdersService
                .createCasePaymentOrder(createCasePaymentOrderRequest, userToken);
            assertThat("UUID does not match", caseOrderReturn.getId().equals(id));
        }
    }
}
