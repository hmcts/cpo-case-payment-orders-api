package uk.gov.hmcts.reform.cpo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersFilterException;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CasePaymentOrderQueryFilterTest implements BaseTest {

    private final List<String> casesIds = List.of("1609243447569251", "1609243447569252", "1609243447569253");

    private final List<String> ids = List.of("df54651b-3227-4067-9f23-6ffb32e2c6bd",
                                             "d702ef36-0ca7-46e9-8a00-ef044d78453e",
                                             "d702ef36-0ca7-46e9-8a00-ef044d78453e");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private CasePaymentOrderQueryFilter getCasePaymentOrderQueryFilter(final List<String> casesIds,
                                                                       final List<String> ids) {

        return CasePaymentOrderQueryFilter.builder()
            .cpoIds(ids)
            .caseIds(casesIds)
            .pageable(getPageRequest())
            .build();
    }

    @Test
    void passValidation() {

        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = getCasePaymentOrderQueryFilter(
            casesIds,
            Collections.emptyList()
        );
        casePaymentOrderQueryFilter.validateCasePaymentOrdersFiltering();

    }

    @Test
    void failValidation() {
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = getCasePaymentOrderQueryFilter(
            casesIds,
            ids
        );

        assertThatThrownBy(casePaymentOrderQueryFilter::validateCasePaymentOrdersFiltering)
            .isInstanceOf(CasePaymentOrdersFilterException.class)
            .hasMessageContaining(ValidationError.CPO_FILTER_ERROR);
    }

    @Test
    void passPageRequest() {

        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = getCasePaymentOrderQueryFilter(
            casesIds,
            Collections.emptyList()
        );
        final Pageable pageRequest = casePaymentOrderQueryFilter.getPageRequest();

        assertThat(pageRequest.getPageNumber(), is(1));
        assertThat(pageRequest.getPageSize(), is(3));
    }
}
