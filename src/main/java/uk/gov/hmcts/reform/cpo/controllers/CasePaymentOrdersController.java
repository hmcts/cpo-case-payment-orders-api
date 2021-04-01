package uk.gov.hmcts.reform.cpo.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import javax.validation.Valid;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import uk.gov.hmcts.reform.cpo.ApplicationParams;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCaseId;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@RestController
@Validated
@RequestMapping(value = "/api")
public class CasePaymentOrdersController {

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
            message = "Case Payment Order Created"
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

    public CasePaymentOrder createCasePaymentOrderRequest(@Valid @RequestBody CreateCasePaymentOrderRequest
                                                                  requestPayload) {
        return casePaymentOrdersService.createCasePaymentOrder(requestPayload);
    }

    private final CasePaymentOrdersServiceImpl casePaymentOrdersServiceImpl;
    private final ApplicationParams applicationParams;

    public CasePaymentOrdersController(CasePaymentOrdersServiceImpl casePaymentOrdersServiceImpl, ApplicationParams applicationParams) {
        this.casePaymentOrdersServiceImpl = casePaymentOrdersServiceImpl;
        this.applicationParams = applicationParams;
    }


    @GetMapping(value = "case-payment-orders", produces = {"application/json"})
    public Page<CasePaymentOrderEntity> getCasePaymentOrders(@ApiParam(value = "list of ids")
                                                             @ValidCpoId @RequestParam("ids") Optional<List<String>> ids,
                                                             @ApiParam(value = "casesId of ids")
                                                             @ValidCaseId @RequestParam("cases-ids") Optional<List<String>> casesId,
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
        return casePaymentOrdersServiceImpl.getCasePaymentOrders(casePaymentOrderQueryFilter);
    }

    //TODO this is not going to be included in final pr
    @GetMapping(value = "case-payment-orders-test-data", produces = {"application/json"})
    public void createData(){
        casePaymentOrdersServiceImpl.create();
    }
}
