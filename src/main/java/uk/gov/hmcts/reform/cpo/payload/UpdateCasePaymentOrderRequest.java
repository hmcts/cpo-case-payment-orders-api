package uk.gov.hmcts.reform.cpo.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;

import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

@Getter
@Schema(name = "Update Case Payment Order Request")
public class UpdateCasePaymentOrderRequest extends BaseCasePaymentOrderRequest {

    @NotEmpty(message = ValidationError.ID_REQUIRED)
    @ValidCpoId
    @Schema(name = "Case Payment Order ID to update", required = true,
        example = "77d30e7f-ead9-4529-a499-6bf8b0f2d08e")
    private final String id;

    public UpdateCasePaymentOrderRequest(String id,
                                         String caseId,
                                         String action,
                                         String responsibleParty,
                                         String orderReference) {
        super(caseId,
              action,
              responsibleParty,
              orderReference);

        this.id = id;
    }

    @JsonIgnore
    public UUID getUUID() {
        return UUID.fromString(this.id);
    }

}
