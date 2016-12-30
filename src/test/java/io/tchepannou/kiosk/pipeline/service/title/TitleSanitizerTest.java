package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TitleSanitizerTest {

    @Test
    public void testFilter() throws Exception {
        // Given
        final Article article = new Article();
        article.setTitle("a");

        final TitleFilter f1 = mock(TitleFilter.class);
        when(f1.filter(any(), any())).thenReturn("a");

        final TitleFilter f2 = mock(TitleFilter.class);
        when(f2.filter(any(), any())).thenReturn("b");

        final TitleFilter f3 = mock(TitleFilter.class);
        when(f3.filter(any(), any())).thenReturn("c");

        // When
        final TitleSanitizer extractor = new TitleSanitizer(Arrays.asList(f1, f2, f3));

        // Then
        assertThat(extractor.filter(article)).isEqualTo("c");

    }
}
