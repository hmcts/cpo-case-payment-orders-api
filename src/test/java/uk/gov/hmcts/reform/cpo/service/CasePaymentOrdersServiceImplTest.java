package uk.gov.hmcts.reform.cpo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import static org.springframework.test.util.AssertionErrors.assertNotNull;

class CasePaymentOrdersServiceImplTest {

    private CasePaymentOrdersService casePaymentOrdersService;

    @Mock
    private CasePaymentOrderMapper casePaymentOrderMapper;
    @Mock
    private CasePaymentOrdersRepository casePaymentOrdersRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        casePaymentOrdersService = new CasePaymentOrdersServiceImpl(
            casePaymentOrderMapper,
            casePaymentOrdersRepository
        );
    }

    @Test
    void verify() {
        assertNotNull("Class is not null", casePaymentOrdersService != null);
    }
}
