package uk.gov.hmcts.reform.cpo.befta;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.exception.FunctionalTestException;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.ReflectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CasePaymentOrdersTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private static Map<String, String> uniqueStringsPerTestData = new ConcurrentHashMap<>();

    @Override
    public BeftaTestDataLoader getDataLoader() {
        return new DefaultBeftaTestDataLoader() {
            @Override
            protected void doLoadTestData() {
                // NB: no CCD test data load requirements
            }
        };
    }

    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        if (key.toString().equals("GenerateCaseId")) {

            String scenarioTag;
            try {
                scenarioTag = scenarioContext.getParentContext().getCurrentScenarioTag();
            } catch (NullPointerException e) {
                scenarioTag = scenarioContext.getCurrentScenarioTag();
            }

            String caseId = new CaseIdGenerator().generateValidCaseReference();
            uniqueStringsPerTestData.put(scenarioTag,caseId);
            return caseId;
        } else if (key.toString().equals("GetGeneratedCaseId")) {
            String scenarioTag;
            try {
                scenarioTag = scenarioContext.getParentContext().getCurrentScenarioTag();
            } catch (NullPointerException e) {
                scenarioTag = scenarioContext.getCurrentScenarioTag();
            }
            return Long.parseLong(uniqueStringsPerTestData.get(scenarioTag));
        }
        return super.calculateCustomValue(scenarioContext, key);
    }

}
