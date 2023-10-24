package uk.gov.hmcts.reform.cpo.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.cpo.validators.Validator;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCaseId;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Getter
@AllArgsConstructor
@Schema(name = "Common case payment order request properties")
public class BaseCasePaymentOrderRequest {

    @NotEmpty(message = ValidationError.CASE_ID_REQUIRED)
    @ValidCaseId
    @Schema(name = "Case Id for which the record applies", required = true, example = "2061729969689088")
    private String caseId;

    @NotEmpty(message = ValidationError.ACTION_REQUIRED)
    @Schema(name = "Action that initiated the creation of the case payment order", required = true,
        example = "Case Submit")
    private String action;

    @NotEmpty(message = ValidationError.RESPONSIBLE_PARTY_REQUIRED)
    @Schema(name = "Description of the party responsible for the case payment order", required = true,
        example = "Jane Doe")
    private String responsibleParty;

    @NotNull(message = ValidationError.ORDER_REFERENCE_REQUIRED)
    @Pattern(regexp = Validator.ORDER_REFERENCE_RG, message = ValidationError.ORDER_REFERENCE_INVALID)
    @Schema(name = "Description of the Payments system order reference", required = true,
        example = "2021-1122334455667")
    private String orderReference;

}
