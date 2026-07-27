package uk.gov.hmcts.reform.cpo.wiremock.extension;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.springframework.http.HttpHeaders;

// Same issue as here https://github.com/tomakehurst/wiremock/issues/97
public class ConnectionClosedTransformer implements ResponseDefinitionTransformerV2 {

    @Override
    public String getName() {
        return "keep-alive-disabler";
    }

    @Override
    public ResponseDefinition transform(ServeEvent serveEvent) {
        return ResponseDefinitionBuilder.like(serveEvent.getResponseDefinition())
            .withHeader(HttpHeaders.CONNECTION, "close")
            .build();
    }

}
