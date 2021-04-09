package uk.gov.hmcts.reform.cpo.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cpo.ApplicationParams;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.security.AuthError;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCaseId;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Validated
public class CasePaymentOrdersController {

    @SuppressWarnings({"squid:S1075"})
    public static final String CASE_PAYMENT_ORDERS_PATH = "/case-payment-orders";
    public static final String CASE_IDS = "case-ids";
    public static final String IDS = "ids";

    private final CasePaymentOrdersService casePaymentOrdersService;
    private final ApplicationParams applicationParams;



    @Autowired
    public CasePaymentOrdersController(CasePaymentOrdersService casePaymentOrdersService,
                                       ApplicationParams applicationParams) {
        this.casePaymentOrdersService = casePaymentOrdersService;
        this.applicationParams = applicationParams;
    }

    @GetMapping(value = CASE_PAYMENT_ORDERS_PATH, produces = {APPLICATION_JSON_VALUE})
    public Page<CasePaymentOrderEntity> getCasePaymentOrders(@ApiParam("list of ids")
                                                             @ValidCpoId
                                                             @RequestParam(IDS) Optional<List<String>> ids,
                                                             @ApiParam("casesId of ids")
                                                             @ValidCaseId
                                                             @RequestParam("cases-ids") Optional<List<String>> casesId,
                                                             @RequestParam("pageSize") Optional<Integer> pageSize,
                                                             @RequestParam("pageNumber") Optional<Integer> pageNumber

    ) {
        final List<String> listOfIds = ids.orElse(Collections.emptyList());
        final List<String> listOfCasesIds = casesId.orElse(Collections.emptyList());
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
                .listOfIds(listOfIds)
                .listOfCasesIds(listOfCasesIds)
                .pageNumber(pageNumber.orElse(Integer.parseInt(applicationParams.getDefaultPageNumber())))
                .pageSize(pageSize.orElse(Integer.parseInt(applicationParams.getDefaultPageSize())))
                .build();
        return casePaymentOrdersService.getCasePaymentOrders(casePaymentOrderQueryFilter);
    }

    @DeleteMapping(path = CASE_PAYMENT_ORDERS_PATH, params = IDS)
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
    public void deleteCasePaymentOrdersById(@RequestParam(IDS) @Valid
                                                  @NotEmpty(message = ValidationError.IDS_EMPTY)
                                                          List<UUID> ids)
            throws CasePaymentOrderCouldNotBeFoundException {
        casePaymentOrdersService.deleteCasePaymentOrdersByIds(ids);
    }

    @DeleteMapping(path = CASE_PAYMENT_ORDERS_PATH, params = CASE_IDS)
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
                + "\n1) " + ValidationError.CASE_ID_INVALID
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
    public void deleteCasePaymentOrdersByCaseId(@RequestParam(CASE_IDS) @Valid
                                                  @NotEmpty(message = ValidationError.CASE_IDS_EMPTY)
                                                  List<
                                                    @LuhnCheck(message = ValidationError.CASE_ID_INVALID,
                                                        ignoreNonDigitCharacters = false)
                                                    @Size(min = 16, max = 16,
                                                            message = ValidationError.CASE_ID_INVALID_LENGTH)
                                                  String> caseIds) throws CasePaymentOrderCouldNotBeFoundException {
        List<Long> caseIdLongs = caseIds.stream().map(Long::parseLong).collect(Collectors.toList());
        casePaymentOrdersService.deleteCasePaymentOrdersByCaseIds(caseIdLongs);
    }


    @PutMapping(path = CASE_PAYMENT_ORDERS_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update Case Payment Order", notes = "Updates a case payment order")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiResponses({
        @ApiResponse(
            code = 202,
            message = ""
        ),
        @ApiResponse(
            code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.ID_REQUIRED
                + "\n2) " + ValidationError.ID_INVALID
                + "\n3) " + ValidationError.EFFECTIVE_FROM_REQUIRED
                + "\n4) " + ValidationError.CASE_ID_REQUIRED
                + "\n5) " + ValidationError.CASE_ID_INVALID
                + "\n6) " + ValidationError.ORDER_REFERENCE_REQUIRED
                + "\n7) " + ValidationError.ACTION_REQUIRED
                + "\n8) " + ValidationError.RESPONSIBLE_PARTY_REQUIRED,
            response = String.class,
            examples = @Example({
                @ExampleProperty(
                    value = "{\n"
                        + "   \"status\": \"400\",\n"
                        + "   \"error\": \"Bad Request\",\n"
                        + "   \"message\": \"" + ValidationError.ARGUMENT_NOT_VALID + "\",\n"
                        + "   \"path\": \"" + CASE_PAYMENT_ORDERS_PATH + "\",\n"
                        + "   \"details\": [ \""  + ValidationError.ID_REQUIRED + "\" ]\n"
                        + "}",
                    mediaType = APPLICATION_JSON_VALUE)
            })
        ),
        @ApiResponse(
            code = 401,
            message = AuthError.AUTHENTICATION_TOKEN_INVALID
        ),
        @ApiResponse(
            code = 403,
            message = AuthError.UNAUTHORISED_S2S_SERVICE
        ),
        @ApiResponse(
            code = 404,
            message = ValidationError.CPO_NOT_FOUND
        ),
        @ApiResponse(
            code = 409,
            message = ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE
        )
    })
    public CasePaymentOrder updateCasePaymentOrderRequest(@Valid @RequestBody UpdateCasePaymentOrderRequest
                                                                  requestPayload) {
        return casePaymentOrdersService.updateCasePaymentOrder(requestPayload);
    }

}
