package uk.gov.hmcts.reform.cpo.befta;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.exception.FunctionalTestException;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.ReflectionUtils;

import java.util.Map;
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
        } else if (key.toString().startsWith("approximately ")) {
            try {
                String actualSizeFromHeaderStr = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                    "testData.actualResponse.headers.Content-Length");
                String expectedSizeStr = key.toString().replace("approximately ", "");

                int actualSize =  Integer.parseInt(actualSizeFromHeaderStr);
                int expectedSize = Integer.parseInt(expectedSizeStr);

                if (Math.abs(actualSize - expectedSize) < (actualSize * 10 / 100)) {
                    return actualSizeFromHeaderStr;
                }
                return expectedSize;
            } catch (Exception e) {
                throw new FunctionalTestException("Problem checking acceptable response payload: ", e);
            }
        } else if (key.toString().startsWith("contains ")) {
            try {
                String actualValueStr = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                    "testData.actualResponse.body.__plainTextValue__");
                String expectedValueStr = key.toString().replace("contains ", "");

                if (actualValueStr.contains(expectedValueStr)) {
                    return actualValueStr;
                }
                return "expectedValueStr " + expectedValueStr + " not present in response ";
            } catch (Exception e) {
                throw new FunctionalTestException("Problem checking acceptable response payload: ", e);
            }
        }
        return super.calculateCustomValue(scenarioContext, key);
    }

}
