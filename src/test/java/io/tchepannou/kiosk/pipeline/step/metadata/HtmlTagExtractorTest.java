package io.tchepannou.kiosk.pipeline.step.metadata;

import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlTagExtractorTest {

    @Mock
    TextFilter filter;

    @InjectMocks
    HtmlTagExtractor extractor;

    @Test
    public void testExtract() throws Exception {
        // Given
        final String html = IOUtils.toString(getClass().getResourceAsStream("/tag/article.html"));
        final Document doc = Jsoup.parse(html);

        when(filter.filter(anyString())).thenAnswer((inv) -> inv.getArguments()[0]);

        // When
        final List<String> result = extractor.extract(doc);

        // Then
        assertThat(result).contains(
                "Cameroun",
                "Céline Victoria Fotso",
                "Churchill Mambe",
                "Je Wanda Magazine",
                "Développement de soi",
                "Jeunesse",
                "Njorku",
                "Partenariat",
                "Valérie Ayena",
                "Vodafone"
        );
    }
}
