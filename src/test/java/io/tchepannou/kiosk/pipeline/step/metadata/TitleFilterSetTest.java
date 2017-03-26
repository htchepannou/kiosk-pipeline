package io.tchepannou.kiosk.pipeline.step.metadata;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TitleFilterSetTest {

    @Test
    public void testFilter() throws Exception {
        // Given
        final TitleFilter f1 = mock(TitleFilter.class);
        when(f1.filter(any(), any())).thenReturn("a");

        final TitleFilter f2 = mock(TitleFilter.class);
        when(f2.filter(any(), any())).thenReturn("b");

        final TitleFilter f3 = mock(TitleFilter.class);
        when(f3.filter(any(), any())).thenReturn("c");

        final TitleFilterSet filters = new TitleFilterSet(Arrays.asList(f1, f2, f3));

        final Feed feed = new Feed ();

        // When/Then
        assertThat(filters.filter("a", feed)).isEqualTo("c");


    }
}
