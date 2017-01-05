package io.tchepannou.kiosk.pipeline.service.similarity.filter;

import io.tchepannou.kiosk.pipeline.service.similarity.TextFilter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PunctuationTextFilterTest {
    TextFilter filter = new PunctuationTextFilter();

    @Test
    public void shouldRemoveAllPuctuation() throws Exception {
        final String text = "L’agglomération de Paris: Marché de Noël, de Strasbourg - cours de grammaire française! WTF?";

        assertThat(filter.filter(text)).isEqualTo("L’agglomération de Paris  Marché de Noël  de Strasbourg   cours de grammaire française  WTF ");
    }
}
