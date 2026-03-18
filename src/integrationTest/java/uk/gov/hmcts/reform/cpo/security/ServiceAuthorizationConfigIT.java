package uk.gov.hmcts.reform.cpo.security;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

@ActiveProfiles("itest")
class ServiceAuthorizationConfigIT {

    // NB: s2s service config from `application-itest.yaml`
    private static final String S2S_ID = "test_crud_service";

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

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
        ServicePermissionInfo info = new ServicePermissionInfo();
        info.setId(S2S_ID);
        info.setPermission(permissionsWhiteListConfig);

        Set<ConstraintViolation<ServicePermissionInfo>> violations = validator.validate(info);
        assertTrue(violations.isEmpty());
    }

    private void assertApplicationContextBeanFailsToLoadWithWhitelistConfig(String permissionsWhiteListConfig) {

        ServicePermissionInfo info = new ServicePermissionInfo();
        info.setId(S2S_ID);
        info.setPermission(permissionsWhiteListConfig);

        Set<ConstraintViolation<ServicePermissionInfo>> violations = validator.validate(info);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> 
                    violation.getMessage().contains(
                        ValidationError.INVALID_PERMISSION_WHITELIST_VALUE
                    )));
    }

}
