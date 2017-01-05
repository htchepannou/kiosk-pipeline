package io.tchepannou.kiosk.pipeline.service.similarity;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ShingleExtractorTest {
    ShingleExtractor extractor = new ShingleExtractor();

    @Test
    public void testExtract() throws Exception {
        final String text = "L’agglomération de Paris Marché de Noël de Strasbourg cours de grammaire française WTF";

        final List<String> result = extractor.extract(text, 5);

        assertThat(result).containsExactly(
                "L’agglomération de Paris Marché de",
                "de Paris Marché de Noël",
                "Paris Marché de Noël de",
                "Marché de Noël de Strasbourg",
                "de Noël de Strasbourg cours",
                "Noël de Strasbourg cours de",
                "de Strasbourg cours de grammaire",
                "Strasbourg cours de grammaire française",
                "cours de grammaire française WTF"
        );
    }
}
