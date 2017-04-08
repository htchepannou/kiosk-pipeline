package io.tchepannou.kiosk.core.nlp.filter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LowecaseTextFilterTest {
    TextFilter filter = new LowecaseTextFilter();

    @Test
    public void shouldConvertToLowerCase() throws Exception {
        final String text = "L’agglomération de Paris: Marché de Noël, de Strasbourg - cours de grammaire française! WTF?";

        assertThat(filter.filter(text)).isEqualTo("l’agglomération de paris: marché de noël, de strasbourg - cours de grammaire française! wtf?");
    }
}
