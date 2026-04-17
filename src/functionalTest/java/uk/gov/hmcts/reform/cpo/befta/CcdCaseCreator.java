package uk.gov.hmcts.reform.cpo.befta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.befta.exception.FunctionalTestException;

public class CcdCaseCreator {

    private static final String JURISDICTION = "BEFTA_JURISDICTION_1";
    private static final String CASE_TYPE = "BEFTA_CASETYPE_1_1";
    private static final String CREATE_EVENT = "CREATE";

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CcdCaseCreator() {
        this.restClient = RestClient.builder().build();
    }

    public String createCase() {
        try {
            String idamToken = getIdamToken();
            String userId = getUserId(idamToken);
            String serviceToken = getServiceToken();
            final String eventToken = getEventToken(idamToken, serviceToken, userId);

            var requestNode = objectMapper.createObjectNode();
            var eventNode = requestNode.putObject("event");
            eventNode.put("id", CREATE_EVENT);
            eventNode.put("summary", "");
            eventNode.put("description", "");

            requestNode.put("event_token", eventToken);
            requestNode.put("ignore_warning", false);
            requestNode.putNull("draft_id");
            requestNode.putObject("data");

            String requestBody = objectMapper.writeValueAsString(requestNode);


            String response = restClient.post()
                .uri(getRequiredEnv("CCD_DATA_STORE_URL") + "/case-types/" + CASE_TYPE + "/cases")
                .header("Authorization", "Bearer " + idamToken)
                .header("ServiceAuthorization", serviceToken)
                .header("experimental", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

            return readJson(response).get("id").asText();
        } catch (Exception ex) {
            throw new FunctionalTestException("Failed to create CCD case for functional test", ex);
        }
    }

    private String getIdamToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", getOAuthClientId());
        form.add("client_secret", getOAuthClientSecret());
        form.add("redirect_uri", getOAuthRedirectUri());
        form.add("scope", getOAuthScope());
        form.add("username", getFtUsername());
        form.add("password", getFtPassword());

        String response = restClient.post()
            .uri(getIdamBaseUrl() + "/o/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(String.class);

        return readJson(response).get("access_token").asText();
    }

    private String getUserId(String idamToken) {
        String response = restClient.get()
            .uri(getIdamBaseUrl() + "/details")
            .header("Authorization", "Bearer " + idamToken)
            .retrieve()
            .body(String.class);

        return readJson(response).get("id").asText();
    }

    private String getServiceToken() {
        String s2sClientId = getFirstDefined("BEFTA_S2S_CLIENT_ID", "BEFTA_S2S_CLIENT_ID_OF_XUI_WEBAPP");
        String response = restClient.post()
            .uri(getS2sBaseUrl() + "/testing-support/lease")
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"microservice\":\"" + s2sClientId + "\"}")
            .retrieve()
            .body(String.class);

        return response;
    }

    private String getEventToken(String idamToken, String serviceToken, String userId) {
        String response = restClient.get()
            .uri(getRequiredEnv("CCD_DATA_STORE_URL")
                     + "/caseworkers/" + userId
                     + "/jurisdictions/" + JURISDICTION
                     + "/case-types/" + CASE_TYPE
                     + "/event-triggers/" + CREATE_EVENT + "/token")
            .header("Authorization", "Bearer " + idamToken)
            .header("ServiceAuthorization", serviceToken)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .body(String.class);

        return readJson(response).get("token").asText();
    }

    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new FunctionalTestException("Failed to parse JSON response", ex);
        }
    }

    private String getFtUsername() {
        return "befta.caseworker.1@gmail.com";
    }

    private String getFtPassword() {
        return getRequiredEnv("CCD_BEFTA_CASEWORKER_1_PWD");
    }

    private String getIdamBaseUrl() {
        return getFirstDefined("IDAM_API_URL", "IDAM_API_URL_BASE", "IDAM_URL");
    }

    private String getS2sBaseUrl() {
        return getFirstDefined("S2S_URL_BASE", "S2S_URL");
    }

    private String getOAuthClientId() {
        return getFirstDefined("BEFTA_OAUTH2_CLIENT_ID_OF_XUIWEBAPP", "OAUTH2_CLIENT_ID");
    }

    private String getOAuthClientSecret() {
        return getFirstDefined("BEFTA_OAUTH2_CLIENT_SECRET_OF_XUIWEBAPP", "OAUTH2_CLIENT_SECRET");
    }

    private String getOAuthRedirectUri() {
        return getFirstDefined("BEFTA_OAUTH2_REDIRECT_URI_OF_XUIWEBAPP", "OAUTH2_REDIRECT_URI");
    }

    private String getOAuthScope() {
        return getFirstDefined("BEFTA_OAUTH2_SCOPE_VARIABLES_OF_XUIWEBAPP", "OAUTH2_SCOPE_VARIABLES");
    }

    private String getRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new FunctionalTestException("Missing required environment variable: " + name);
        }
        return value;
    }

    private String getFirstDefined(String... names) {
        for (String name : names) {
            String value = System.getenv(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new FunctionalTestException("Missing required environment variable from: " + String.join(", ", names));
    }
}
