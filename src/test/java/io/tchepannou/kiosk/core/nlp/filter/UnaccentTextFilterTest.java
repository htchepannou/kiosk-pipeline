package io.tchepannou.kiosk.core.nlp.filter;

import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import io.tchepannou.kiosk.core.nlp.filter.UnaccentTextFilter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnaccentTextFilterTest {
    TextFilter filter = new UnaccentTextFilter();

    @Test
    public void shouldRemoveAllAccents() throws Exception {
        final String text = "L’agglomération de Paris. Marché de Noël de Strasbourg - cours de grammaire française";

        assertThat(filter.filter(text)).isEqualTo("L’agglomeration de Paris. Marche de Noel de Strasbourg - cours de grammaire francaise");
    }
}
