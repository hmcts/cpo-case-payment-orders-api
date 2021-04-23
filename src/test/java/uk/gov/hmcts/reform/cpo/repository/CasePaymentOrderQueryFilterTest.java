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

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CasePaymentOrderQueryFilterTest implements BaseTest {

    private final List<String> casesIds = createInitialValuesList(new String[]{"1609243447569251",
        "1609243447569252", "1609243447569253"}).get();

    private final List<String> ids = createInitialValuesList(new String[]{"df54651b-3227-4067-9f23-6ffb32e2c6bd",
        "d702ef36-0ca7-46e9-8a00-ef044d78453e",
        "d702ef36-0ca7-46e9-8a00-ef044d78453e"}).get();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

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
        try {
            final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter = getCasePaymentOrderQueryFilter(
                casesIds,
                ids
            );
            casePaymentOrderQueryFilter.validateCasePaymentOrdersFiltering();
            fail();
        } catch (CasePaymentOrdersFilterException casePaymentOrdersQueryException) {
            assertThat(casePaymentOrdersQueryException.getMessage(), is(ValidationError.CPO_FILER_ERROR));
        }

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
