package uk.gov.hmcts.reform.cpo.security;

import lombok.Data;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import jakarta.validation.constraints.Pattern;

@Data
class ServicePermissionInfo {
    private String id;

    @Pattern(regexp = "^[crudCRUD]+", message = ValidationError.INVALID_PERMISSION_WHITELIST_VALUE)
    private String permission;
}
