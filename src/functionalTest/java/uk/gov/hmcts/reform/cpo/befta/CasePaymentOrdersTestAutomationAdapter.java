package uk.gov.hmcts.reform.cpo.befta;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;

public class CasePaymentOrdersTestAutomationAdapter extends DefaultTestAutomationAdapter {

    @Override
    public BeftaTestDataLoader getDataLoader() {
        return new DefaultBeftaTestDataLoader() {
            @Override
            protected void doLoadTestData() {
                // NB: no CCD test data load requirements
            }
        };
    }
}
