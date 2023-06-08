package uk.gov.hmcts.reform.cpo.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "Create Case Payment Order Request")
public class CreateCasePaymentOrderRequest extends BaseCasePaymentOrderRequest {

    public CreateCasePaymentOrderRequest(String caseId,
                                         String action,
                                         String responsibleParty,
                                         String orderReference) {
        super(caseId,
              action,
              responsibleParty,
              orderReference);
    }

}
