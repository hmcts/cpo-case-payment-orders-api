package uk.gov.hmcts.reform.cpo.controllers;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import uk.gov.hmcts.reform.cpo.ApplicationParams;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.security.AuthError;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCaseId;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@RestController
@Validated
public class CasePaymentOrdersController {

    @SuppressWarnings({"squid:S1075"})
    public static final String CASE_PAYMENT_ORDERS_PATH = "/case-payment-orders";

    private final CasePaymentOrdersService casePaymentOrdersService;
    private final ApplicationParams applicationParams;

    public CasePaymentOrdersController(CasePaymentOrdersService casePaymentOrdersService,
                                       ApplicationParams applicationParams) {
        this.casePaymentOrdersService = casePaymentOrdersService;
        this.applicationParams = applicationParams;
    }

    @PostMapping(path = CASE_PAYMENT_ORDERS_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create Case Payment Order", notes = "Create Case Payment Order")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
        @ApiResponse(
            code = 201,
            message = "Case Payment Order Created"
        ),
        @ApiResponse(
            code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.CASE_ID_INVALID
                + "\n2) " + ValidationError.CASE_ID_REQUIRED
                + "\n3) " + ValidationError.CASE_ID_INVALID_LENGTH
                + "\n4) " + ValidationError.EFFECTIVE_FROM_REQUIRED
                + "\n5) " + ValidationError.ACTION_REQUIRED
                + "\n6) " + ValidationError.RESPONSIBLE_PARTY_REQUIRED
                + "\n7) " + ValidationError.ORDER_REFERENCE_REQUIRED
                + "\n8) " + ValidationError.ORDER_REFERENCE_INVALID
                + "\n9) " + ValidationError.IDAM_ID_NOT_FOUND,
            response = String.class,
            examples = @Example({
                @ExampleProperty(
                    value = "{\n"
                        + "   \"status\": \"400\",\n"
                        + "   \"error\": \"Bad Request\",\n"
                        + "   \"message\": \"" + ValidationError.ARGUMENT_NOT_VALID + "\",\n"
                        + "   \"path\": \"" + CASE_PAYMENT_ORDERS_PATH + "\",\n"
                        + "   \"details\": [ \""  + ValidationError.CASE_ID_INVALID + "\" ]\n"
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
            code = 409,
            message = ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE
        ),
        @ApiResponse(
            code = 500,
            message = "Unexpected server error"
        )
    })

    public CasePaymentOrder createCasePaymentOrderRequest(@Valid @RequestBody CreateCasePaymentOrderRequest
                                                              requestPayload) {
        return casePaymentOrdersService.createCasePaymentOrder(requestPayload);
    }

    @GetMapping(value = "case-payment-orders", produces = {"application/json"})
    @ApiOperation(value = "Get payment orders for a case", notes = "Get payment orders for a case")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = ""
        ),
        @ApiResponse(
            code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.CPO_FILER_ERROR
                + "\n2) " + ValidationError.CASE_ID_INVALID
                + "\n3) " + ValidationError.ID_INVALID,
            response = String.class,
            examples = @Example({
                @ExampleProperty(
                    value = "{\n"
                        + "   \"status\": \"400\",\n"
                        + "   \"error\": \"Bad Request\",\n"
                        + "   \"message\": \"" + ValidationError.ARGUMENT_NOT_VALID + "\",\n"
                        + "   \"path\": \"" + CASE_PAYMENT_ORDERS_PATH + "\",\n"
                        + "   \"details\": [ \"" + ValidationError.CASE_ID_INVALID + "\" ]\n"
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
        )
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "page number", paramType = "query"),
        @ApiImplicitParam(name = "size", value = "page size", paramType = "query")
    })
    public Page<CasePaymentOrder> getCasePaymentOrders(@ApiParam("list of case payment orders ids")
                                                       @ValidCpoId
                                                       @RequestParam(name = "ids", required = false)
                                                           Optional<List<String>> ids,
                                                       @ApiParam("list of ccd case reference numbers")
                                                       @ValidCaseId
                                                       @RequestParam(name = "case-ids", required = false)
                                                           Optional<List<String>> caseIds,
                                                       @ApiIgnore("This is ignored by swagger") Pageable pageable

    ) {

        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
            .cpoIds(ids.orElse(Collections.emptyList()))
            .caseIds(caseIds.orElse(Collections.emptyList()))
            .pageable(pageable)
            .build();

        if (casePaymentOrderQueryFilter.noFilters()) {
            return Page.empty();
        }
        casePaymentOrderQueryFilter.validateCasePaymentOrdersFiltering();
        return casePaymentOrdersService.getCasePaymentOrders(casePaymentOrderQueryFilter);
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
