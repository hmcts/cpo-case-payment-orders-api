package uk.gov.hmcts.reform.cpo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
@Getter
public class ApplicationParams {

    @Value("${cpo.defaultPageSize}")
    private String defaultPageSize;

    @Value("${cpo.defaultPageNumber}")
    private String defaultPageNumber;
}
