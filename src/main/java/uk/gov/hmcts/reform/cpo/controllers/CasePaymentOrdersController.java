package uk.gov.hmcts.reform.cpo.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cpo.ApplicationParams;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.security.AuthError;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCaseId;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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


    @GetMapping(value = CASE_PAYMENT_ORDERS_PATH, produces = {APPLICATION_JSON_VALUE})
    public Page<CasePaymentOrderEntity> getCasePaymentOrders(@ApiParam("list of ids")
                                                             @ValidCpoId
                                                             @RequestParam("ids") Optional<List<String>> ids,
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
