package uk.gov.hmcts.reform.cpo.payload;

import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@ApiModel("Create Case Payment Order Request")
public class CreateCasePaymentOrderRequest extends BaseCasePaymentOrderRequest {

    public CreateCasePaymentOrderRequest(LocalDateTime effectiveFrom,
                                         String caseId,
                                         String action,
                                         String responsibleParty,
                                         String orderReference) {
        super(effectiveFrom,
              caseId,
              action,
              responsibleParty,
              orderReference);
    }

}