package uk.gov.hmcts.reform;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface BaseTest {

    default <T> Optional<List<T>> createInitialValuesList(final T[] initialValues) {
        return Optional.of(Arrays.asList(initialValues));
    }
}
