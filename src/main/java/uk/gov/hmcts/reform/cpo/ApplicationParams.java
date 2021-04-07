package uk.gov.hmcts.reform.cpo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApplicationParams {

    @Value("${cpo.defaultPageSize}")
    private Integer defaultPageSize;
}
