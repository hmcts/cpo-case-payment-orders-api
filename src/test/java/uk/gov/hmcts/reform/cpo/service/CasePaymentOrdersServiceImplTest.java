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
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersQueryException;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

class CasePaymentOrdersServiceImplTest implements BaseTest<String> {

    private CasePaymentOrdersService casePaymentOrdersService;


    private final List<String> casesIds = createInitialValuesList(new String[]{"1609243447569251",
        "1609243447569252", "1609243447569253"}).get();

    private final List<String> ids = createInitialValuesList(new String[]{"df54651b-3227-4067-9f23-6ffb32e2c6bd",
        "d702ef36-0ca7-46e9-8a00-ef044d78453e",
        "d702ef36-0ca7-46e9-8a00-ef044d78453e"}).get();

    private static final Integer PAGE_NUMBER = 0;
    private static final Integer PAGE_SIZE = 5;

    @Mock
    private CasePaymentOrderMapper casePaymentOrderMapper;
    @Mock
    private CasePaymentOrdersRepository casePaymentOrdersRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        casePaymentOrdersService = new CasePaymentOrdersServiceImpl(casePaymentOrderMapper,
                                                                    casePaymentOrdersRepository);
    }

    @Test
    void passForEmptyPages() {
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
            .listOfIds(Collections.emptyList())
            .listOfCasesIds(Collections.emptyList())
            .pageNumber(PAGE_NUMBER)
            .pageSize(PAGE_SIZE)
            .build();

        final Page<CasePaymentOrderEntity> pages = casePaymentOrdersService.getCasePaymentOrders(
            casePaymentOrderQueryFilter);
        assertTrue("The getNumberOfElements should be 0.", pages.getNumberOfElements() == 0);
    }


    @Test
    void failForCasesIdsAndIdsTogether() {
        final String expectedError = "case-payment-orders cannot filter case payments orders by both id and cases-id.";
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
            .listOfIds(ids)
            .listOfCasesIds(casesIds)
            .pageNumber(PAGE_NUMBER)
            .pageSize(PAGE_SIZE)
            .build();
        try{
            casePaymentOrdersService.getCasePaymentOrders(casePaymentOrderQueryFilter);
            fail();
        }catch (CasePaymentOrdersQueryException casePaymentOrdersQueryException){
            assertThat(casePaymentOrdersQueryException.getMessage(), is(expectedError));
        }
    }

    @Test
    void passForListIds() {
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
            .listOfIds(ids)
            .listOfCasesIds(Collections.emptyList())
            .pageNumber(PAGE_NUMBER)
            .pageSize(PAGE_SIZE)
            .build();

        when(casePaymentOrdersRepository.findByIdIn(anyList(), ArgumentMatchers.<Pageable>any())).thenReturn(
            getPages(casePaymentOrderQueryFilter));
        final Page<CasePaymentOrderEntity> pages = casePaymentOrdersService.getCasePaymentOrders(
            casePaymentOrderQueryFilter);

        assertTrue("The getNumberOfElements should be 0.", pages.getNumberOfElements() == 3);
    }

    @Test
    void passForListCasesIds() {
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
            .listOfIds(Collections.emptyList())
            .listOfCasesIds(casesIds)
            .pageNumber(PAGE_NUMBER)
            .pageSize(PAGE_SIZE)
            .build();

        when(casePaymentOrdersRepository.findByCaseIdIn(anyList(), ArgumentMatchers.<Pageable>any())).thenReturn(
            getPages(casePaymentOrderQueryFilter));
        final Page<CasePaymentOrderEntity> pages = casePaymentOrdersService.getCasePaymentOrders(
            casePaymentOrderQueryFilter);

        assertTrue("The getNumberOfElements should be 0.", pages.getNumberOfElements() == 3);
    }

    private Page<CasePaymentOrderEntity> getPages(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        final PageRequest pageRequest = getPageRequest(casePaymentOrderQueryFilter);
        return new PageImpl<CasePaymentOrderEntity>(createListOfCasePaymentOrderEntity(), pageRequest, 3);
    }

    private PageRequest getPageRequest(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        return PageRequest.of(
            casePaymentOrderQueryFilter.getPageNumber(),
            casePaymentOrderQueryFilter.getPageSize()
        );
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
        casePaymentOrderEntity.setCaseTypeId("setCaseTypeId");
        casePaymentOrderEntity.setResponsibleParty("setResponsibleParty");
        casePaymentOrders.add(casePaymentOrderEntity);

        final CasePaymentOrderEntity casePaymentOrderEntity1 = new CasePaymentOrderEntity();
        casePaymentOrderEntity1.setAction("action");
        casePaymentOrderEntity1.setCaseId(Long.parseLong("1609243447569252"));
        casePaymentOrderEntity1.setCreatedBy("action1");
        casePaymentOrderEntity1.setOrderReference("Baction2");
        casePaymentOrderEntity1.setEffectiveFrom(LocalDateTime.now());
        casePaymentOrderEntity1.setCreatedTimestamp(LocalDateTime.now());
        casePaymentOrderEntity1.setCaseTypeId("setCaseTypeId");
        casePaymentOrderEntity1.setResponsibleParty("setResponsibleParty");
        casePaymentOrders.add(casePaymentOrderEntity1);

        final CasePaymentOrderEntity casePaymentOrderEntity2 = new CasePaymentOrderEntity();
        casePaymentOrderEntity2.setAction("action");
        casePaymentOrderEntity2.setCaseId(Long.parseLong("1609243447569253"));
        casePaymentOrderEntity2.setCreatedBy("action1");
        casePaymentOrderEntity2.setOrderReference("Caction3");
        casePaymentOrderEntity2.setEffectiveFrom(LocalDateTime.now());
        casePaymentOrderEntity2.setCreatedTimestamp(LocalDateTime.now());
        casePaymentOrderEntity2.setCaseTypeId("setCaseTypeId");
        casePaymentOrderEntity2.setResponsibleParty("setResponsibleParty");
        casePaymentOrders.add(casePaymentOrderEntity2);
        return casePaymentOrders;
    }
}
