package uk.gov.hmcts.reform.cpo.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cpo.security.Permission.CREATE;
import static uk.gov.hmcts.reform.cpo.security.Permission.DELETE;
import static uk.gov.hmcts.reform.cpo.security.Permission.READ;
import static uk.gov.hmcts.reform.cpo.security.Permission.UPDATE;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = ServiceAuthorizationConfig.class)
@ActiveProfiles("serviceAuthPermissions")
class ServiceAuthorizationConfigTest {

    private static final String TAKE_MY_MONEY_APP = "take_my_money_app";

    private static final String USER_INTERFACE_WEBAPP = "user_interface_webapp";

    private static final String ANOTHER_SERVICE = "anotherService";

    @Autowired
    private ServiceAuthorizationConfig serviceAuthorizationConfig;

    @Test
    void testHasPermissionsForUnknownServiceReturnsFalse() {
        assertFalse(serviceAuthorizationConfig.hasPermissions("unknownService", CREATE));
    }

    @Test
    void testHasPermissions_ReturnsFalse_CheckReadOnlyAppHasCreatePermission() {
        assertFalse(serviceAuthorizationConfig.hasPermissions(TAKE_MY_MONEY_APP, CREATE));
    }

    @Test
    void testHasPermissions_ReturnsTrue_CheckReadOnlyAppHasReadPermission() {
        assertTrue(serviceAuthorizationConfig.hasPermissions(TAKE_MY_MONEY_APP, READ));
    }

    @Test
    void testHasPermissions_ReturnsFalse_CheckUpdateAndDeleteAppHasCreatePermission() {
        assertFalse(serviceAuthorizationConfig.hasPermissions(ANOTHER_SERVICE, CREATE));
    }

    @Test
    void testHasPermissions_ReturnsTrue_CheckUpdateAndDeleteAppHasDeletePermission() {
        assertTrue(serviceAuthorizationConfig.hasPermissions(ANOTHER_SERVICE, DELETE));
    }

    @Test
    void testHasPermissions_ReturnsTrue_Check_CRUD_AppHasAllPermissions() {
        assertAll(
            () -> assertTrue(serviceAuthorizationConfig.hasPermissions(USER_INTERFACE_WEBAPP, CREATE)),
            () -> assertTrue(serviceAuthorizationConfig.hasPermissions(USER_INTERFACE_WEBAPP, READ)),
            () -> assertTrue(serviceAuthorizationConfig.hasPermissions(USER_INTERFACE_WEBAPP, UPDATE)),
            () -> assertTrue(serviceAuthorizationConfig.hasPermissions(USER_INTERFACE_WEBAPP, DELETE))
        );
    }
}
