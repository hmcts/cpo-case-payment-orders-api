package uk.gov.hmcts.reform.cpo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.time.Clock;

@SpringBootApplication
@ComponentScan("uk.gov.hmcts.reform")
@EnableFeignClients(basePackageClasses = IdamApi.class)
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Clock utcClock() {
        return Clock.systemUTC();
    }

}
