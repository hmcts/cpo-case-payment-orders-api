package uk.gov.hmcts.reform.cpo.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.cpo.controllers.RootController;
import uk.gov.hmcts.reform.cpo.exception.RestExceptionHandler;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

/** Real loopback HTTP, with no credentials, database, or upstream IDAM/S2S calls. */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = UnauthenticatedHttpIT.HttpApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "management.health.defaults.enabled=false",
        "management.health.livenessstate.enabled=true",
        "management.health.readinessstate.enabled=true",
        "management.endpoint.health.cache.time-to-live=0ms"
    })
class UnauthenticatedHttpIT {

    private static final String PRIVATE = "private-case-123";
    private static final String QUERIES = "?trace=true&message=true&errors=true&exception=true";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();

    @LocalServerPort
    private int port;

    @Autowired
    private AtomicBoolean unhealthy;

    @MockitoBean
    private JwtDecoder decoder;

    @MockitoBean
    private JwtGrantedAuthoritiesConverter converter;

    @MockitoBean
    private ServiceAuthorisationApi serviceApi;

    @MockitoBean
    private IdamClient idamClient;

    @BeforeEach
    void captureLogs() {
        unhealthy.set(false);
        logs.start();
        root.addAppender(logs);
    }

    @AfterEach
    void verifyNoDiagnosticLogsOrRemoteCalls() {
        root.detachAppender(logs);
        logs.stop();
        unhealthy.set(false);
        assertThat(logs.list).filteredOn(event -> event.getLevel().isGreaterOrEqual(Level.WARN))
            .allSatisfy(event -> {
                assertThat(event.getFormattedMessage()).doesNotContain(PRIVATE, "java.lang.", "jdbc:");
                assertThat(event.getThrowableProxy()).isNull();
            });
        verifyNoInteractions(decoder, converter, serviceApi, idamClient);
    }

    @Test
    void publicEndpointsRemainAvailableWithoutCredentials() throws Exception {
        for (String path : new String[] {"/", "/info", "/health", "/health/liveness", "/health/readiness"}) {
            assertThat(send("GET", path + QUERIES, "application/json", null).statusCode()).isEqualTo(200);
        }
        unhealthy.set(true);
        HttpResponse<String> response = send("GET", "/health" + QUERIES, "application/json", null);
        assertThat(response.statusCode()).isEqualTo(503);
        assertSafe(response);
        assertThat(response.body()).doesNotContain("components", "details");
    }

    @Test
    void servletFallbackPreserves500ForJsonAndHtml() throws Exception {
        for (String path : new String[] {"/swagger-ui/local-error/" + PRIVATE, "/swagger-ui/local-filter-error"}) {
            for (String accept : new String[] {"application/json", "text/html"}) {
                HttpResponse<String> response = send("GET", path + QUERIES, accept, null);
                assertThat(response.statusCode()).isEqualTo(500);
                assertSafe(response);
                assertThat(response.headers().firstValue("Content-Type").orElseThrow()).startsWith(accept);
                if (accept.equals("text/html")) {
                    assertThat(response.body()).contains("Request failed");
                }
            }
        }
    }

    @Test
    void xmlOnlyFallbackRetainsItsStatusWithoutDiagnostics() throws Exception {
        HttpResponse<String> response = send("GET", "/swagger-ui/local-error/" + PRIVATE + QUERIES,
            "application/xml", null);
        assertThat(response.statusCode()).isEqualTo(500);
        assertSafe(response);
        assertThat(response.body()).isEmpty();
    }

    @Test
    void controllerDenialsRetainSafeBearerChallenges() throws Exception {
        HttpResponse<String> response = send("GET", "/swagger-ui/local-denied" + QUERIES, "application/json", null);
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.headers().firstValue("WWW-Authenticate").orElseThrow())
            .isEqualTo("Bearer error=\"insufficient_scope\"");
        assertSafe(response);
    }

    @Test
    void malformedBodiesMethodsAndMissingPublicResourcesStaySafe() throws Exception {
        HttpResponse<String> malformed = send("POST", "/swagger-ui/local-json" + QUERIES,
            "application/json", "{\"" + PRIVATE + "\":");
        assertThat(malformed.statusCode()).isEqualTo(400);
        assertSafe(malformed);
        HttpResponse<String> method = send("POST", "/" + QUERIES, "application/json", null);
        assertThat(method.statusCode()).isEqualTo(405);
        assertThat(method.headers().firstValue("Allow").orElseThrow()).contains("GET");
        assertSafe(method);
        for (String accept : new String[] {"application/json", "text/html"}) {
            HttpResponse<String> missing = send("GET", "/swagger-ui/missing-" + PRIVATE + QUERIES, accept, null);
            assertThat(missing.statusCode()).isEqualTo(404);
            assertSafe(missing);
        }
    }

    @Test
    void protectedEndpointsStillRejectAnonymousRequests() throws Exception {
        for (String path : new String[] {"/case-payment-orders", "/error"}) {
            HttpResponse<String> response = send("GET", path + QUERIES, "application/json", null);
            assertThat(response.statusCode()).isEqualTo(401);
            assertThat(response.body()).isEmpty();
        }
    }

    @Test
    void malformedRequestTargetsDoNotExposeContainerDiagnostics() throws Exception {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(5000);
            String request = "GET /swagger-ui/%ZZ-" + PRIVATE + " HTTP/1.1\r\nHost: localhost\r\n"
                + "Accept: text/html\r\nConnection: close\r\n\r\n";
            socket.getOutputStream().write(request.getBytes(StandardCharsets.US_ASCII));
            String response = new String(socket.getInputStream().readNBytes(16384), StandardCharsets.UTF_8);
            assertThat(response).startsWith("HTTP/1.1 400");
            assertThat(response).doesNotContain(PRIVATE, "IllegalArgumentException", "org.apache.", "Exception Report");
        }
    }

    private HttpResponse<String> send(String method, String path, String accept, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .timeout(Duration.ofSeconds(5)).header("Accept", accept).header("Content-Type", "application/json")
            .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body))
            .build(), HttpResponse.BodyHandlers.ofString());
    }

    private void assertSafe(HttpResponse<String> response) {
        assertThat(response.body()).doesNotContain(PRIVATE, "java.lang.", "\"exception\"", "\"trace\"", "\"path\"",
            "Whitelabel", "Exception Report");
    }

    @Configuration(proxyBeanMethods = false)
    @TestComponent
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class})
    @Import({RootController.class, ProbeController.class, SecurityConfiguration.class,
        SafeServiceAuthConfiguration.class,
        ErrorPageConfiguration.class, RestExceptionHandler.class, SafeHealthLoggingConfiguration.class})
    static class HttpApplication {
        @Bean
        AtomicBoolean unhealthy() {
            return new AtomicBoolean();
        }

        @Bean
        HealthIndicator localHealth(AtomicBoolean unhealthy) {
            return () -> unhealthy.get()
                ? Health.down(new IllegalStateException(PRIVATE)).withDetail("private", PRIVATE).build()
                : Health.up().build();
        }

        @Bean
        FilterRegistrationBean<jakarta.servlet.Filter> failingFilter() {
            var registration = new FilterRegistrationBean<jakarta.servlet.Filter>();
            registration.setFilter((request, response, chain) -> {
                throw new jakarta.servlet.ServletException(PRIVATE);
            });
            registration.addUrlPatterns("/swagger-ui/local-filter-error");
            return registration;
        }
    }

    @RestController
    @TestComponent
    static class ProbeController {
        @GetMapping("/swagger-ui/local-denied")
        void deny() {
            throw new org.springframework.security.access.AccessDeniedException(PRIVATE);
        }

        @GetMapping("/swagger-ui/local-error/{id}")
        void error(HttpServletResponse response) throws java.io.IOException {
            response.sendError(500, PRIVATE);
        }

        @PostMapping("/swagger-ui/local-json")
        Map<String, String> echo(@RequestBody Map<String, String> body) {
            return body;
        }
    }
}
