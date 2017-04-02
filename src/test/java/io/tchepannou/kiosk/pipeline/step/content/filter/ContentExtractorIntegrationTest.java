package io.tchepannou.kiosk.pipeline.step.content.filter;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContentExtractorIntegrationTest {
    @Autowired
    ContentExtractor extractor;

    @Test
    public void shouldFilterHotJem() throws Exception {
        testFilter("hotjem");
    }

    @Test
    public void shouldFilterJeWanda() throws Exception {
        testFilter("jewanda");
    }

    @Test
    public void shouldFilterJeWanda2() throws Exception {
        testFilter("jewanda2");
    }

    @Test
    public void shouldFilterCulturEbene() throws Exception {
        testFilter("culturebene");
    }

    @Test
    public void shouldFilterCamer24() throws Exception {
        testFilter("camer24");
    }

    @Test
    public void shouldFilterAucunlait() throws Exception {
        testFilter("aucunlait");
    }

    @Test
    public void shouldFilterCameroonOnline() throws Exception {
        testFilter("cameroononline");
    }

    private void testFilter(final String name) throws Exception {
        // Given
        final String html = IOUtils.toString(getClass().getResourceAsStream("/extractor/content_" + name + ".html"));
        final String expected = IOUtils.toString(getClass().getResourceAsStream("/extractor/content_" + name + "_filtered.html"));

        // When
        final String result = extractor.extract(html);

        // Then
        final String resultText = Jsoup.parse(result).text();
        final String expectedText = Jsoup.parse(expected).text();
        assertThat(resultText).isEqualTo(expectedText);
    }

}
