package io.tchepannou.kiosk.pipeline.service.content;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentFilterTest {
    private final ContentFilter filter = new ContentFilter(100);

    @Test
    public void shouldFilter() throws Exception {
        testFilter("default");
    }


    @Test
    public void shouldFilterMboaFootball() throws Exception {
        testFilter("mboafootball");
    }

    private void testFilter(final String name) throws Exception {
        // Given
        final String html = IOUtils.toString(getClass().getResourceAsStream("/extractor/content_" + name + ".html"));
        final String expected = IOUtils.toString(getClass().getResourceAsStream("/extractor/content_" + name + "_filtered.html"));

        // When
        final String result = filter.filter(html);

        // Then
        final String resultText = Jsoup.parse(result).text();
        final String expectedText = Jsoup.parse(expected).text();
        assertThat(resultText).isEqualTo(expectedText);
    }
}
