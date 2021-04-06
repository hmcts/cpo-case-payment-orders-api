package uk.gov.hmcts.reform.cpo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;

import static org.springframework.test.util.AssertionErrors.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CasePaymentOrdersServiceImplTest {

    private CasePaymentOrdersService casePaymentOrdersService;

    @Mock
    private CasePaymentOrdersRepository casePaymentOrdersRepository;

    @BeforeEach
    public void setUp() {
        casePaymentOrdersService = new CasePaymentOrdersServiceImpl(
            casePaymentOrdersRepository
        );
    }

    @Test
    void verify() {
        assertNotNull("Class is not null", casePaymentOrdersService != null);
    }
}
