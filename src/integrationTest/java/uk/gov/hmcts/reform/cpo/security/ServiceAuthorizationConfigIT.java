package uk.gov.hmcts.reform.cpo.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("itest")
class ServiceAuthorizationConfigIT {

    // NB: s2s service config from `application-itest.yaml`
    private static final String S2S_ID = "test_crud_service";
    private static final String S2S_ID_PROPERTY = "S2S.AUTHORIZATIONS.TEST_CRUD_SERVICE.ID";
    private static final String S2S_PERMISSION_PROPERTY = "S2S.AUTHORIZATIONS.TEST_CRUD_SERVICE.PERMISSION";

    private ApplicationContextRunner applicationContextRunner;

    @Test
    void testServiceAuthorizationConfigBean_loadsSuccesfully_create_whitelistPermissions() {
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("C");
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("c");
    }

    @Test
    void testServiceAuthorizationConfigBean_loadsSuccesfully_read_whitelistPermissions() {
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("R");
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("r");
    }

    @Test
    void testServiceAuthorizationConfigBean_loadsSuccesfully_update_whitelistPermissions() {
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("U");
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("u");
    }

    @Test
    void testServiceAuthorizationConfigBean_loadsSuccesfully_delete_whitelistPermissions() {
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("D");
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("d");
    }

    @Test
    void testServiceAuthorizationConfigBean_loadsSuccesfully_withValidUpperCase_whitelistPermissions() {
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("CRUD");
    }

    @Test
    void testServiceAuthorizationConfigBean_loadsSuccesfully_withValidLowerCase_whitelistPermissions() {
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("durc");
    }

    @Test
    void testServiceAuthorizationConfigBean_loadsSuccesfully_withValidMixedCase_whitelistPermissions() {
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("dRUc");
    }

    @Test
    void testServiceAuthorizationConfigBean_loadsSuccesfully_withValid_createcreatecreate_whitelistPermissions() {
        assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig("CCC");
    }

    @Test
    void testServiceAuthorizationConfigBean_failsToLoad_withInvalid_whitelistPermission() {
        assertApplicationContextBeanFailsToLoadWithWhitelistConfig("CRUDE");
    }

    @Test
    void testServiceAuthorizationConfigBean_failsToLoad_withInvalid_whitelistPermissions() {
        assertApplicationContextBeanFailsToLoadWithWhitelistConfig("CaRbUcD");
    }

    @Test
    void testServiceAuthorizationConfigBean_failsToLoad_withNo_whitelistPermissions() {
        assertApplicationContextBeanFailsToLoadWithWhitelistConfig("");
    }

    @Test
    void testServiceAuthorizationConfigBean_failsToLoad_withInvalidCommas_whitelistPermissions() {
        assertApplicationContextBeanFailsToLoadWithWhitelistConfig("C,R,U,D");
    }

    @Test
    void testServiceAuthorizationConfigBean_failsToLoad_withInvalidSpaces_whitelistPermissions() {
        assertApplicationContextBeanFailsToLoadWithWhitelistConfig("C R U D");
    }

    private void assertApplicationContextBeanLoadedSuccessfullyWithWhitelistConfig(String permissionsWhiteListConfig) {
        applicationContextRunner = createApplicationContextRunner(permissionsWhiteListConfig);

        applicationContextRunner.run(context ->
            assertNotNull(context.getBean(ServiceAuthorizationConfig.class))
        );
    }

    private void assertApplicationContextBeanFailsToLoadWithWhitelistConfig(String permissionsWhiteListConfig) {
        applicationContextRunner = createApplicationContextRunner(permissionsWhiteListConfig);


        applicationContextRunner.run(context -> {
                Exception exception =
                        assertThrows(IllegalStateException.class,
                            () -> context.getBean(ServiceAuthorizationConfig.class));

                Optional<Throwable> rootCause = Stream.iterate(exception, Throwable::getCause)
                        .filter(throwable -> throwable instanceof BindValidationException)
                        .filter(throwable -> ((BindValidationException)throwable).getValidationErrors().hasErrors())
                        .findFirst();
                assertTrue(rootCause.get().getMessage()
                            .contains(ValidationError.INVALID_PERMISSION_WHITELIST_VALUE));
            }
        );
    }

    private ApplicationContextRunner createApplicationContextRunner(String permissionValues) {

        return new ApplicationContextRunner()
                .withUserConfiguration(ServiceAuthorizationConfig.class, ConfigurationPropertiesAutoConfiguration.class)
                .withPropertyValues(S2S_ID_PROPERTY + ":" + S2S_ID,
                                    S2S_PERMISSION_PROPERTY + ":" + permissionValues);
    }
}
