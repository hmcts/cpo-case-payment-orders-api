package uk.gov.hmcts.reform.cpo.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;


import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
public class CasePaymentOrdersController {

    @SuppressWarnings({"squid:S1075"})

    private static final Logger LOG = LoggerFactory.getLogger(CasePaymentOrdersController.class);
    public static final String CASE_PAYMENT_ORDERS_PATH = "/case-payment-orders";

    private final CasePaymentOrdersService casePaymentOrdersService;

    public CasePaymentOrdersController(CasePaymentOrdersService casePaymentOrdersService) {
        this.casePaymentOrdersService = casePaymentOrdersService;
    }


    @PostMapping(path = CASE_PAYMENT_ORDERS_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create Case Payment Order", notes = "Create Case Payment Order")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
        @ApiResponse(
            code = 201,
            message = ""
        ),
        @ApiResponse(
            code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " //+ ValidationError.CASE_ID_INVALID
                + "\n2) " //+ ValidationError.CASE_ID_INVALID_LENGTH
                + "\n3) "//+ ValidationError.CASE_ID_EMPTY
                + "\n4) " //+ ValidationError.CASE_TYPE_ID_EMPTY
                + "\n5) " //+ ValidationError.ASSIGNEE_ID_EMPTY
                + "\n6) " //+ ValidationError.ASSIGNEE_ORGANISATION_ERROR
                + "\n7) " //+ ValidationError.ORGANISATION_POLICY_ERROR
                + "\n8) ", //+ ValidationError.ASSIGNEE_ROLE_ERROR,
            response = String.class,
            examples = @Example({
                @ExampleProperty(
                    value = "{\n"
                        + "   \"status\": \"BAD_REQUEST\",\n"
                        + "   \"message\": \"" //+ ValidationError.ASSIGNEE_ORGANISATION_ERROR + "\",\n"
                        + "   \"errors\": [ ]\n"
                        + "}",
                    mediaType = APPLICATION_JSON_VALUE)
            })
        ),
        @ApiResponse(
            code = 401,
            message = ""
        ),
        @ApiResponse(
            code = 403,
            message = ""
        ),
        @ApiResponse(
            code = 500,
            message = ""
        )
    })

    public CasePaymentOrder createCasePaymentOrderRequest(@RequestBody CreateCasePaymentOrderRequest requestPayload,
                                                          @RequestHeader(AUTHORIZATION) String authorizationHeader) {
        LOG.info(String.valueOf(requestPayload), authorizationHeader);
        return casePaymentOrdersService.createCasePaymentOrder(requestPayload, authorizationHeader);
    }
}
