package uk.gov.hmcts.reform.cpo.payload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ApiModel("Create Case Payment Order Request")
public class CreateCasePaymentOrderRequest {

    @ApiModelProperty(value = "The date/time from which the record is valid", required = true,
        example = "2021-02-10T03:02:30Z")
    private LocalDateTime effectiveFrom;

    @ApiModelProperty(value = "Case Id for which the record applies", required = true, example = "1612345678123456")
    private Long caseId;

    @ApiModelProperty(value = "Action that initiated the creation of the case payment order", required = true,
        example = "Case Submit")
    private String action;

    @ApiModelProperty(value = "Description of the party responsible for the case payment order", required = true,
        example = "Jane Doe")
    private String responsibleParty;

    @ApiModelProperty(value = "Description of the Payments system order reference", required = true,
        example = "2021-918425346")
    private String orderReference;
}
