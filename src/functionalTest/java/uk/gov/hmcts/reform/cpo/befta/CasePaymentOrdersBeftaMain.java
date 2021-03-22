package uk.gov.hmcts.reform.cpo.befta;

import uk.gov.hmcts.befta.BeftaMain;

public class CasePaymentOrdersBeftaMain extends BeftaMain {

    public static void main(String[] args) {
        BeftaMain.main(args, new CasePaymentOrdersTestAutomationAdapter());
    }

}
