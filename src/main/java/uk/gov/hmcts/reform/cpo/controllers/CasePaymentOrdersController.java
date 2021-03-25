package uk.gov.hmcts.reform.cpo.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cpo.errorhandling.AuthError;
import uk.gov.hmcts.reform.cpo.errorhandling.ValidationError;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Validated
public class CasePaymentOrdersController {

    @DeleteMapping(path = "/case-payment-orders", params = "ids")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete case payment orders by id", notes = "Delete case payment orders by id")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = ""
        ),
        @ApiResponse(
            code = 400,
            message = ValidationError.IDS_EMPTY,
            examples = @Example(value = {
                @ExampleProperty(
                    value = "{\n"
                        + "   \"status\": \"BAD_REQUEST\",\n"
                        + "   \"errors\": [\n"
                        + "   \"message\": \"" + ValidationError.IDS_EMPTY + "\",\n"
                        + "   ]\n"
                        + "}",
                    mediaType = APPLICATION_JSON_VALUE
                )
            })
        ),
        @ApiResponse(
            code = 401,
            message = AuthError.AUTHENTICATION_TOKEN_INVALID
        ),
        @ApiResponse(
            code = 403,
            message = AuthError.UNAUTHORISED_S2S_SERVICE
        )
    })
    public void deleteCasePaymentOrdersById(@RequestParam("ids") @Valid
                                                  @NotEmpty(message = ValidationError.IDS_EMPTY)
                                                          List<UUID> ids) {
    }

    @DeleteMapping(path = "/case-payment-orders", params = "case-ids")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete case payment orders by case-ids", notes = "Delete case payment orders by case-ids")
    @ApiResponses({
        @ApiResponse(
                code = 200,
                message = ""
        ),
        @ApiResponse(
            code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.CASE_IDS_INVALID
                + "\n2) " + ValidationError.CASE_ID_INVALID_LENGTH
                + "\n3) " + ValidationError.CASE_IDS_EMPTY,
            examples = @Example(value = {
                @ExampleProperty(
                    value = "{\n"
                            + "   \"status\": \"BAD_REQUEST\",\n"
                            + "   \"errors\": [\n"
                            + "      \"case-ids can not be empty,\"\n"
                            + "      \"caseId invalid,\"\n"
                            + "      \"invalid length\"\n"
                            + "   ]\n"
                            + "}",
                    mediaType = APPLICATION_JSON_VALUE
                )
            })
        ),
        @ApiResponse(
            code = 401,
            message = AuthError.AUTHENTICATION_TOKEN_INVALID
        ),
        @ApiResponse(
            code = 403,
            message = AuthError.UNAUTHORISED_S2S_SERVICE
        )
    })
    public void deleteCasePaymentOrdersByCaseId(@RequestParam("case-ids") @Valid
                                                  @NotEmpty(message = ValidationError.CASE_IDS_EMPTY)
                                                  List<
                                                    @LuhnCheck(message = ValidationError.CASE_IDS_INVALID,
                                                        ignoreNonDigitCharacters = false)
                                                    @Size(min = 16, max = 16,
                                                            message = ValidationError.CASE_ID_INVALID_LENGTH)
                                                  String> caseIds) {
    }
}
