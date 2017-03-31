package io.tchepannou.kiosk.pipeline.step.content.filter;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContentExtractorIntegrationTest {
    @Autowired
    ContentExtractor extractor;

    public void shouldFilterHotJem() throws Exception {
        testFilter("hotjem");
    }

    public void shouldFilterJeWanda() throws Exception {
        testFilter("jewanda");
    }

    public void shouldFilterJeWanda2() throws Exception {
        testFilter("jewanda2");
    }

    public void shouldFilterCulturEbene() throws Exception {
        testFilter("culturebene");
    }

    public void shouldFilterCamer24() throws Exception {
        testFilter("camer24");
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
