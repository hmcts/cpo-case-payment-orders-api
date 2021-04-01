package uk.gov.hmcts.reform.cpo.controllers;

import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    private final CasePaymentOrdersServiceImpl casePaymentOrdersServiceImpl;
    private final ApplicationParams applicationParams;

    public CasePaymentOrdersController(CasePaymentOrdersServiceImpl casePaymentOrdersServiceImpl,
                                       ApplicationParams applicationParams) {
        this.casePaymentOrdersServiceImpl = casePaymentOrdersServiceImpl;
        this.applicationParams = applicationParams;
    }


    @GetMapping(value = "case-payment-orders", produces = {"application/json"})
    public Page<CasePaymentOrderEntity> getCasePaymentOrders(@ApiParam(value = "list of ids")
                                                             @ValidCpoId
                                                             @RequestParam("ids") Optional<List<String>> ids,
                                                             @ApiParam(value = "casesId of ids")
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
        return casePaymentOrdersServiceImpl.getCasePaymentOrders(casePaymentOrderQueryFilter);
    }

    //TODO this is not going to be included in final pr
    @GetMapping(value = "case-payment-orders-test-data", produces = {"application/json"})
    public void createData() {
        casePaymentOrdersServiceImpl.create();
    }
}
