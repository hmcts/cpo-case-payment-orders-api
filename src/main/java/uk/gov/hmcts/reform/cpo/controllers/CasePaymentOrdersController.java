package uk.gov.hmcts.reform.cpo.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cpo.auditlog.AuditOperationType;
import uk.gov.hmcts.reform.cpo.auditlog.LogAudit;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.security.AuthError;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCaseId;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Validated
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ConditionalOnProperty(value = "case.payment.orders.api.enabled", havingValue = "true")
public class CasePaymentOrdersController {

    @SuppressWarnings({"squid:S1075"})
    public static final String CASE_PAYMENT_ORDERS_PATH = "/case-payment-orders";
    public static final String CASE_IDS = "case_ids";
    public static final String IDS = "ids";

    private final CasePaymentOrdersService casePaymentOrdersService;


    public CasePaymentOrdersController(CasePaymentOrdersService casePaymentOrdersService) {
        this.casePaymentOrdersService = casePaymentOrdersService;
    }

    @PostMapping(path = CASE_PAYMENT_ORDERS_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Case Payment Order", description = "Create Case Payment Order")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Case Payment Order Created"
            ),
        @ApiResponse(
            responseCode = "400",
            description = "One or more of the following reasons:"
                + "\n1) " + ValidationError.CASE_ID_INVALID
                + "\n2) " + ValidationError.CASE_ID_REQUIRED
                + "\n3) " + ValidationError.ACTION_REQUIRED
                + "\n4) " + ValidationError.RESPONSIBLE_PARTY_REQUIRED
                + "\n5) " + ValidationError.ORDER_REFERENCE_INVALID
                + "\n6) " + ValidationError.ORDER_REFERENCE_REQUIRED
                + "\n7) " + ValidationError.IDAM_ID_RETRIEVE_ERROR,
            content = @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(
                value = "{\n"
                    + "   \"status\": \"400\",\n"
                    + "   \"error\": \"Bad Request\",\n"
                    + "   \"message\": \"" + ValidationError.ARGUMENT_NOT_VALID + "\",\n"
                    + "   \"path\": \"" + CASE_PAYMENT_ORDERS_PATH + "\",\n"
                    + "   \"details\": [ \"" + ValidationError.CASE_ID_INVALID + "\" ]\n"
                    + "}"
            ))
            ),
        @ApiResponse(
            responseCode = "401",
            description = AuthError.AUTHENTICATION_TOKEN_INVALID
            ),
        @ApiResponse(
            responseCode = "403",
            description = AuthError.UNAUTHORISED_S2S_SERVICE
            ),
        @ApiResponse(
            responseCode = "409",
            description = ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE
            )
    })
    @LogAudit(
        operationType = AuditOperationType.CREATE_CASE_PAYMENT_ORDER,
        cpoId = "#result.id",
        caseId = "#requestPayload.caseId"
    )
    @PreAuthorize("@securityUtils.hasCreatePermission()")
    public CasePaymentOrder createCasePaymentOrderRequest(@Valid @RequestBody CreateCasePaymentOrderRequest
                                                              requestPayload) {
        return casePaymentOrdersService.createCasePaymentOrder(requestPayload);
    }

    @GetMapping(path = CASE_PAYMENT_ORDERS_PATH, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get payment orders for a case", description = "Get payment orders for a case")
    @ApiResponses({
        @ApiResponse(responseCode = "200"),
        @ApiResponse(
            responseCode = "400",
            description = "One or more of the following reasons:"
                + "\n1) " + ValidationError.CPO_FILTER_ERROR
                + "\n2) " + ValidationError.CASE_ID_INVALID
                + "\n3) " + ValidationError.ID_INVALID,
            content = @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(
                value = "{\n"
                    + "   \"status\": \"400\",\n"
                    + "   \"error\": \"Bad Request\",\n"
                    + "   \"message\": \"" + ValidationError.ARGUMENT_NOT_VALID + "\",\n"
                    + "   \"path\": \"" + CASE_PAYMENT_ORDERS_PATH + "\",\n"
                    + "   \"details\": [ \"" + ValidationError.CASE_ID_INVALID + "\" ]\n"
                    + "}"
            ))
            ),
        @ApiResponse(
            responseCode = "401",
            description = AuthError.AUTHENTICATION_TOKEN_INVALID
            ),
        @ApiResponse(
            responseCode = "403",
            description = AuthError.UNAUTHORISED_S2S_SERVICE
            ),
        @ApiResponse(
            responseCode = "404",
            description = ValidationError.CPOS_NOT_FOUND
            ),
    })
    @Parameters({
        @Parameter(name = "page", description = "page number, indexes from (0,1) to page-size.",
            in = ParameterIn.QUERY),
        @Parameter(name = "size", description = "page size", in = ParameterIn.QUERY)
    })
    @LogAudit(
        operationType = AuditOperationType.GET_CASE_PAYMENT_ORDER,
        cpoIds = "T(uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController).buildOptionalIds(#ids)",
        caseIds  = "T(uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController).buildOptionalIds(#caseIds)"
    )
    @PreAuthorize("@securityUtils.hasReadPermission()")
    public Page<CasePaymentOrder> getCasePaymentOrders(@Parameter(description = "list of case payment orders ids")
                                                       @ValidCpoId
                                                       @RequestParam(name = IDS, required = false)
                                                           Optional<List<String>> ids,
                                                       @Parameter(description = "list of ccd case reference numbers")
                                                       @ValidCaseId
                                                       @RequestParam(name = CASE_IDS, required = false)
                                                           Optional<List<String>> caseIds,
                                                       @Parameter(hidden = true) Pageable pageable

    ) {

        final var casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
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

    @DeleteMapping(path = CASE_PAYMENT_ORDERS_PATH, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete specified case payment orders")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Case Payment Orders Deleted"
            ),
        @ApiResponse(
            responseCode = "400",
            description = "One or more of the following reasons:"
                + "\n1) " + ValidationError.ID_INVALID
                + "\n2) " + ValidationError.CASE_ID_INVALID
                + "\n3) " + ValidationError.CANNOT_DELETE_USING_IDS_AND_CASE_IDS,
            content = @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(
                value = "{\n"
                    + "   \"status\": \"400\",\n"
                    + "   \"error\": \"Bad Request\",\n"
                    + "   \"message\": \"" + ValidationError.ARGUMENT_NOT_VALID + "\",\n"
                    + "   \"path\": \"" + CASE_PAYMENT_ORDERS_PATH + "\",\n"
                    + "   \"details\": [ \"" + ValidationError.CANNOT_DELETE_USING_IDS_AND_CASE_IDS + "\" ]\n"
                    + "}"
            ))
            ),
        @ApiResponse(
            responseCode = "401",
            description = AuthError.AUTHENTICATION_TOKEN_INVALID
            ),
        @ApiResponse(
            responseCode = "403",
            description = AuthError.UNAUTHORISED_S2S_SERVICE
            ),
        @ApiResponse(
            responseCode = "404",
            description = ValidationError.CPOS_NOT_FOUND
            )
    })
    @LogAudit(
        operationType = AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
        cpoIds = "T(uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController).buildOptionalIds(#ids)",
        caseIds  = "T(uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController).buildOptionalIds(#caseIds)"
    )
    @PreAuthorize("@securityUtils.hasDeletePermission()")
    public void deleteCasePaymentOrdersById(@Parameter(description = "list of IDs")
                                            @ValidCpoId
                                            @RequestParam(name = IDS, required = false) Optional<List<String>> ids,
                                            @Parameter(description = "list of Case IDs")
                                            @ValidCaseId
                                            @RequestParam(name = CASE_IDS, required = false)
                                                Optional<List<String>> caseIds) {

        final var casePaymentOrderQueryFilter = CasePaymentOrderQueryFilter.builder()
            .cpoIds(ids.orElse(emptyList()))
            .caseIds(caseIds.orElse(emptyList()))
            .build();

        casePaymentOrdersService.deleteCasePaymentOrders(casePaymentOrderQueryFilter);
    }

    @PutMapping(path = CASE_PAYMENT_ORDERS_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update Case Payment Order", description = "Updates a case payment order")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiResponses({
        @ApiResponse(
            responseCode = "202",
            description = "Case Payment Order Updated"
            ),
        @ApiResponse(
            responseCode = "400",
            description = "One or more of the following reasons:"
                + "\n1) " + ValidationError.ID_INVALID
                + "\n2) " + ValidationError.ID_REQUIRED
                + "\n3) " + ValidationError.CASE_ID_INVALID
                + "\n4) " + ValidationError.CASE_ID_REQUIRED
                + "\n5) " + ValidationError.ACTION_REQUIRED
                + "\n6) " + ValidationError.RESPONSIBLE_PARTY_REQUIRED
                + "\n7) " + ValidationError.ORDER_REFERENCE_INVALID
                + "\n8) " + ValidationError.ORDER_REFERENCE_REQUIRED
                + "\n9) " + ValidationError.IDAM_ID_RETRIEVE_ERROR,
            content = @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(
                value = "{\n"
                    + "   \"status\": \"400\",\n"
                    + "   \"error\": \"Bad Request\",\n"
                    + "   \"message\": \"" + ValidationError.ARGUMENT_NOT_VALID + "\",\n"
                    + "   \"path\": \"" + CASE_PAYMENT_ORDERS_PATH + "\",\n"
                    + "   \"details\": [ \"" + ValidationError.ID_REQUIRED + "\" ]\n"
                    + "}"
            ))
            ),
        @ApiResponse(
            responseCode = "401",
            description = AuthError.AUTHENTICATION_TOKEN_INVALID
            ),
        @ApiResponse(
            responseCode = "403",
            description = AuthError.UNAUTHORISED_S2S_SERVICE
            ),
        @ApiResponse(
            responseCode = "404",
            description = ValidationError.CPO_NOT_FOUND
            ),
        @ApiResponse(
            responseCode = "409",
            description = ValidationError.CASE_ID_ORDER_REFERENCE_UNIQUE
            )
    })
    @LogAudit(
        operationType = AuditOperationType.UPDATE_CASE_PAYMENT_ORDER,
        cpoId = "#requestPayload.id",
        caseId = "#requestPayload.caseId"
    )
    @PreAuthorize("@securityUtils.hasUpdatePermission()")
    public CasePaymentOrder updateCasePaymentOrderRequest(@Valid @RequestBody UpdateCasePaymentOrderRequest
                                                              requestPayload) {
        return casePaymentOrdersService.updateCasePaymentOrder(requestPayload);
    }

    public static List<String> buildOptionalIds(Optional<List<String>> optionalIds) {
        return optionalIds.orElseGet(Lists::newArrayList);
    }

}
