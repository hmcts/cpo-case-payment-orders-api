package uk.gov.hmcts.reform.cpo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
@Getter
public class ApplicationParams {

    @Value("${ccd.defaultPageSize}")
    private String defaultPageSize;

    @Value("${ccd.defaultPageNumber}")
    private String defaultPageNumber;
}
