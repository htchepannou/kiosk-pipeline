package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleSanitizer;
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
        final Feed feed = new Feed();
        final Link link = new Link();
        link.setFeed(feed);
        final Article article = new Article();
        article.setLink(link);
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

    @Test
    public void testFilterNullTitle() throws Exception {
        // Given
        final Article article = new Article();
        article.setTitle(null);

        final TitleFilter f1 = mock(TitleFilter.class);
        when(f1.filter(any(), any())).thenReturn("a");

        final TitleFilter f2 = mock(TitleFilter.class);
        when(f2.filter(any(), any())).thenReturn("b");

        final TitleFilter f3 = mock(TitleFilter.class);
        when(f3.filter(any(), any())).thenReturn("c");

        // When
        final TitleSanitizer extractor = new TitleSanitizer(Arrays.asList(f1, f2, f3));

        // Then
        assertThat(extractor.filter(article)).isNull();

    }
}
