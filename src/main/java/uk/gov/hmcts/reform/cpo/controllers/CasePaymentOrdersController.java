package uk.gov.hmcts.reform.cpo.controllers;

import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api")
public class CasePaymentOrdersController {

    private final CasePaymentOrdersServiceImpl casePaymentOrdersServiceImpl;

    public CasePaymentOrdersController(CasePaymentOrdersServiceImpl casePaymentOrdersServiceImpl) {
        this.casePaymentOrdersServiceImpl = casePaymentOrdersServiceImpl;
    }


    @GetMapping(value = "cases/payments/orders", produces = {"application/json"})
    public List<CasePaymentOrder> getCasePaymentOrders(@ApiParam(value = "list of ids")
                                                       @RequestParam("ids") Optional<List<String>> ids,
                                                       @ApiParam(value = "list of ids")
                                                       @RequestParam("ids") Optional<List<String>> casesId,
                                                       @RequestParam("ids") Optional<Integer> pageSize,
                                                       @RequestParam("ids") Optional<Integer> pageNumber

    ){
        return casePaymentOrdersServiceImpl.getCasePaymentOrders(
            ids,
            casesId,
            pageSize.orElse(CasePaymentOrdersServiceImpl.DEFAULT_PAGE_SIZE),
            pageNumber.orElse(CasePaymentOrdersServiceImpl.DEFAULT_PAGE_NUMBER)
        );
    }

}
