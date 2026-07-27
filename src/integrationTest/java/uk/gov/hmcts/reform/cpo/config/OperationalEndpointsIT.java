package uk.gov.hmcts.reform.cpo.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.cpo.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OperationalEndpointsIT extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {
        "/health",
        "/health/liveness",
        "/health/readiness",
        "/info",
        "/v3/api-docs",
        "/swagger-ui/index.html"
    })
    void shouldExposeOperationalEndpointsWithoutAuthentication(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint))
            .andExpect(status().isOk());
    }
}
