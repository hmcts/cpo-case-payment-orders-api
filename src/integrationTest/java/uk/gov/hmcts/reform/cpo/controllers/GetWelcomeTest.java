package uk.gov.hmcts.reform.cpo.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetWelcomeTest extends BaseTest {

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private CasePaymentOrdersRepository repo;

    @DisplayName("Should welcome upon root request with 200 response code")
    @Test
    @Transactional
    public void welcomeRootEndpoint() throws Exception {
        MvcResult response = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();

        assertThat(response.getResponse().getContentAsString()).startsWith("Welcome");

        CasePaymentOrderEntity entity = new CasePaymentOrderEntity();
        entity.setAction("action");
        entity.setCaseId(1234L);
        entity.setCaseTypeId("CaseType");
        entity.setEffectiveFrom(LocalDateTime.now(Clock.systemUTC()));
        entity.setCreatedBy("someone");
        entity.setOrderReference("ref");
        entity.setResponsibleParty("responsible");

        repo.save(entity);

        List<CasePaymentOrderEntity> entities = repo.findAll();
        System.out.println("we have some.. " + entities.size());
    }
}
