package uk.gov.hmcts.reform.cpo.payload;

//import javax.validation.constraints.NotEmpty;
//import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ApiModel("CasePaymentOrder Request")
public class CasePaymentOrderRequest {

    @JsonProperty("effective_from")
    //@NotEmpty(message = ValidationError.ASSIGNEE_ID_EMPTY)
    //@ApiModelProperty(value = "IDAM ID of the Assign User", required = true, example = "ecb5edf4-2f5f-4031-a0ec")
    private LocalDateTime effectiveFrom;

    @JsonProperty("case_id")
    //@NotEmpty(message = ValidationError.CASE_ID_EMPTY)
    //@Size(min = 16, max = 16, message = ValidationError.CASE_ID_INVALID_LENGTH)
    //@LuhnCheck(message = ValidationError.CASE_ID_INVALID, ignoreNonDigitCharacters = false)
    //@ApiModelProperty(value = "Case ID to Assign Access To", required = true, example = "1583841721773828")
    private Long caseId;

    @JsonProperty("case_type_id")
    //@NotEmpty(message = ValidationError.CASE_TYPE_ID_EMPTY)
    //@ApiModelProperty(value = "Case type ID of the requested case", required = true, example = "PROBATE-TEST")
    private String caseTypeId;

    @JsonProperty("action")
    //@NotEmpty(message = ValidationError.ASSIGNEE_ID_EMPTY)
    //@ApiModelProperty(value = "IDAM ID of the Assign User", required = true, example = "ecb5edf4-2f5f-4031-a0ec")
    private String action;

    @JsonProperty("responsible_party")
    //@NotEmpty(message = ValidationError.ASSIGNEE_ID_EMPTY)
    //@ApiModelProperty(value = "IDAM ID of the Assign User", required = true, example = "ecb5edf4-2f5f-4031-a0ec")
    private String responsibleParty;

    @JsonProperty("order_reference")
    //@NotEmpty(message = ValidationError.ASSIGNEE_ID_EMPTY)
    //@ApiModelProperty(value = "IDAM ID of the Assign User", required = true, example = "ecb5edf4-2f5f-4031-a0ec")
    private String orderReference;

    @JsonProperty("User_token")
    //@NotEmpty(message = ValidationError.ASSIGNEE_ID_EMPTY)
    //@ApiModelProperty(value = "IDAM ID of the Assign User", required = true, example = "ecb5edf4-2f5f-4031-a0ec")
    private String userToken;
}
