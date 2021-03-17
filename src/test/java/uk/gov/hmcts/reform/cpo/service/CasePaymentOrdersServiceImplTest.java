package uk.gov.hmcts.reform.cpo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;

import static org.springframework.test.util.AssertionErrors.assertNotNull;

class CasePaymentOrdersServiceImplTest {

    private CasePaymentOrdersService casePaymentOrdersService;

    @BeforeEach
    public void setUp() {
        casePaymentOrdersService = new CasePaymentOrdersServiceImpl();
    }

    @Test
    void verify() {
        assertNotNull("Class is not null", casePaymentOrdersService != null);
    }
}
