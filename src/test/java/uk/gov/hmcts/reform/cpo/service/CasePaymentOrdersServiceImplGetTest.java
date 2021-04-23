package uk.gov.hmcts.reform.cpo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

class CasePaymentOrdersServiceImplGetTest implements BaseTest {

    private CasePaymentOrdersService casePaymentOrdersService;


    private final List<String> casesIds = createInitialValuesList(new String[]{"1609243447569251",
        "1609243447569252", "1609243447569253"}).get();

    private final List<String> ids = createInitialValuesList(new String[]{"df54651b-3227-4067-9f23-6ffb32e2c6bd",
        "d702ef36-0ca7-46e9-8a00-ef044d78453e",
        "d702ef36-0ca7-46e9-8a00-ef044d78453e"}).get();

    @Mock
    private CasePaymentOrderMapper casePaymentOrderMapper;
    @Mock
    private CasePaymentOrdersRepository casePaymentOrdersRepository;
    @Mock
    private SecurityUtils securityUtils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        casePaymentOrdersService = new CasePaymentOrdersServiceImpl(
            casePaymentOrdersRepository,
            securityUtils,
            casePaymentOrderMapper
        );
    }


    @Test
    void passForListIds() {
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
            .cpoIds(ids)
            .caseIds(Collections.emptyList())
            .pageable(getPageRequest())
            .build();

        when(casePaymentOrdersRepository.findByIdIn(anyList(), ArgumentMatchers.<Pageable>any())).thenReturn(
            getEntityPages());

        when(casePaymentOrderMapper.toDomainModelList(anyList())).thenReturn(createListOfCasePaymentOrder());
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

        when(casePaymentOrderMapper.toDomainModelList(anyList())).thenReturn(createListOfCasePaymentOrder());

        final Page<CasePaymentOrder> pages = casePaymentOrdersService.getCasePaymentOrders(
            casePaymentOrderQueryFilter);

        assertTrue("The getNumberOfElements should be 0.", pages.getNumberOfElements() == 3);
    }

    @Test
    void failForListCasesIds() {
        final ArrayList<CasePaymentOrderEntity> casePaymentOrders = new ArrayList<>();
        final Page<CasePaymentOrderEntity> pageImpl = new PageImpl<CasePaymentOrderEntity>(
            casePaymentOrders,
            getPageRequest(),
            3
        );
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
            .cpoIds(Collections.emptyList())
            .caseIds(casesIds)
            .pageable(getPageRequest())
            .build();

        when(casePaymentOrdersRepository.findByCaseIdIn(anyList(), ArgumentMatchers.<Pageable>any())).thenReturn(
            pageImpl);


        final Page<CasePaymentOrder> pages;
        try {
            casePaymentOrdersService.getCasePaymentOrders(
                casePaymentOrderQueryFilter);
        } catch (CasePaymentOrderCouldNotBeFoundException exception) {
            assertTrue(
                "The error message was not the expected.",
                "Case Payment Order does not exist.".equals(exception.getMessage())
            );
            return;
        }
        fail();
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