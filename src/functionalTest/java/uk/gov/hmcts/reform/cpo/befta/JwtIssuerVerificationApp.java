package uk.gov.hmcts.reform.cpo.befta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

public final class JwtIssuerVerificationApp {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JwtIssuerVerificationApp() {
    }

    public static void main(String[] args) throws Exception {
        verifyIssuerAlignment();
    }

    static void verifyIssuerAlignment() throws Exception {
        String expectedIssuer = requireEnv("OIDC_ISSUER");
        String accessToken = fetchAccessToken();
        String actualIssuer = decodeIssuer(accessToken);

        if (!expectedIssuer.equals(actualIssuer)) {
            throw new IllegalStateException(
                "OIDC_ISSUER mismatch: expected `" + expectedIssuer + "` but token iss was `" + actualIssuer + "`"
            );
        }

        System.out.println("Verified OIDC_ISSUER matches functional test token iss: " + actualIssuer);
    }

    static void verifyIssuerAlignmentIfEnabled() {
        if (!Boolean.parseBoolean(System.getenv("VERIFY_OIDC_ISSUER"))) {
            return;
        }
        try {
            verifyIssuerAlignment();
        } catch (Exception e) {
            throw new IllegalStateException("Functional test JWT issuer verification failed before authenticated setup",
                e);
        }
    }

    private static String fetchAccessToken() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        String requestBody = formUrlEncoded(Map.of(
            "client_id", requireEnv("OAUTH2_CLIENT_ID"),
            "client_secret", requireEnv("OAUTH2_CLIENT_SECRET"),
            "grant_type", "password",
            "username", requireEnv("CCD_CASEWORKER_AUTOTEST_EMAIL"),
            "password", requireEnv("CCD_CASEWORKER_AUTOTEST_PASSWORD"),
            "scope", requireEnv("OAUTH2_SCOPE_VARIABLES")
        ));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(requireEnv("IDAM_API_URL_BASE") + "/o/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Failed to obtain IDAM access token: HTTP " + response.statusCode());
        }

        JsonNode body = OBJECT_MAPPER.readTree(response.body());
        JsonNode accessToken = body.get("access_token");
        if (accessToken == null || accessToken.isNull() || accessToken.asText().isBlank()) {
            throw new IllegalStateException("IDAM token response did not contain access_token");
        }
        return accessToken.asText();
    }

    private static String decodeIssuer(String accessToken) throws Exception {
        String[] parts = accessToken.split("\\.");
        if (parts.length < 2) {
            throw new IllegalStateException("Access token is not a JWT");
        }

        byte[] decodedPayload = Base64.getUrlDecoder().decode(padBase64(parts[1]));
        JsonNode payload = OBJECT_MAPPER.readTree(new String(decodedPayload, StandardCharsets.UTF_8));
        JsonNode issuer = payload.get("iss");
        if (issuer == null || issuer.isNull()) {
            throw new IllegalStateException("Access token does not contain an iss claim");
        }
        return issuer.asText();
    }

    private static String formUrlEncoded(Map<String, String> params) {
        return params.entrySet().stream()
            .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
            .collect(Collectors.joining("&"));
    }

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String padBase64(String value) {
        int remainder = value.length() % 4;
        return remainder == 0 ? value : value + "=".repeat(4 - remainder);
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }
}
