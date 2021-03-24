package uk.gov.hmcts.reform.cpo.repository;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class CasePaymentOrderQueryFilter {

    private Integer pageSize;
    private Integer pageNumber;
    private List<String> listOfIds;
    private List<String> listOfCasesIds;

}
