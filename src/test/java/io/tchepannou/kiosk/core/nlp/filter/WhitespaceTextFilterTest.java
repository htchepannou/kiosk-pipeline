package io.tchepannou.kiosk.core.nlp.filter;

import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import io.tchepannou.kiosk.core.nlp.filter.WhitespaceTextFilter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WhitespaceTextFilterTest {
    TextFilter filter = new WhitespaceTextFilter();

    @Test
    public void testFilter() throws Exception {
        final String text = "L’agglomération de Paris  Marché de Noël  de Strasbourg   cours de grammaire française  WTF ";

        assertThat(filter.filter(text)).isEqualTo("L’agglomération de Paris Marché de Noël de Strasbourg cours de grammaire française WTF");
    }
}
