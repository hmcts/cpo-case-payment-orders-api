package uk.gov.hmcts.reform.cpo.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.reset;

@ExtendWith(SpringExtension.class)
public class FlywayNoOpStrategyTest {

    @Mock
    private Flyway flyway;

    @Mock
    private MigrationInfoService infoService;

    @Mock
    private MigrationInfo info;

    private final FlywayMigrationStrategy strategy = new FlywayNoOpStrategy();

    @AfterEach
    public void tearUp() {
        reset(flyway, infoService, info);
    }

    @Test
    public void shouldNotThrowExceptionWhenAllMigrationsAreApplied() {
        MigrationInfo[] infos = { info, info };
        given(flyway.info()).willReturn(infoService);
        given(infoService.all()).willReturn(infos);
        given(info.getState()).willReturn(MigrationState.SUCCESS);

        Throwable exception = catchThrowable(() -> strategy.migrate(flyway));
        assertThat(exception).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenOneMigrationIsPending() {
        MigrationInfo[] infos = { info, info };
        given(flyway.info()).willReturn(infoService);
        given(infoService.all()).willReturn(infos);
        given(info.getState()).willReturn(MigrationState.SUCCESS, MigrationState.PENDING);

        Throwable exception = catchThrowable(() -> strategy.migrate(flyway));
        assertThat(exception)
                .isInstanceOf(PendingMigrationScriptException.class)
                .hasMessageStartingWith("Found migration not yet applied");
    }
}
