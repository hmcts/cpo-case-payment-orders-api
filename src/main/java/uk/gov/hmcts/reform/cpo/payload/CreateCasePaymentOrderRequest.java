package uk.gov.hmcts.reform.cpo.payload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.LuhnCheck;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
@ApiModel("Create Case Payment Order Request")
public class CreateCasePaymentOrderRequest {

    @NotNull(message = ValidationError.EFFECTIVE_FROM_REQUIRED)
    @ApiModelProperty(value = "The date/time from which the record is valid", required = true,
        example = "2021-02-10T03:02:30Z")
    private LocalDateTime effectiveFrom;

    @NotNull(message = ValidationError.CASE_ID_REQUIRED)
    @Size(min = 16, max = 16, message = ValidationError.CASE_ID_INVALID_LENGTH)
    @LuhnCheck(message = ValidationError.CASE_ID_INVALID)
    @ApiModelProperty(value = "Case Id for which the record applies", required = true, example = "2061729969689088")
    private String caseId;

    @NotEmpty(message = ValidationError.ACTION_REQUIRED)
    @ApiModelProperty(value = "Action that initiated the creation of the case payment order", required = true,
        example = "Case Submit")
    private String action;

    @NotEmpty(message = ValidationError.RESPONSIBLE_PARTY_REQUIRED)
    @ApiModelProperty(value = "Description of the party responsible for the case payment order", required = true,
        example = "Jane Doe")
    private String responsibleParty;

    @Pattern(regexp = "^2[0-9]{3}-[0-9]{11}$", message = ValidationError.ORDER_REFERENCE_INVALID)
    @NotEmpty(message = ValidationError.ORDER_REFERENCE_REQUIRED)
    @ApiModelProperty(value = "Description of the Payments system order reference", required = true,
        example = "2021-11223344556")
    private String orderReference;
}
