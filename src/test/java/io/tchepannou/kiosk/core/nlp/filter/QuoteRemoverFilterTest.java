package io.tchepannou.kiosk.core.nlp.filter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QuoteRemoverFilterTest {
    TextFilter filter = new QuoteRemoverFilter();

    @Test
    public void testFilter() throws Exception {
        assertThat(filter.filter("«semaine.» le document")).isEqualTo("semaine. le document");
        assertThat(filter.filter("semaine.\" le document")).isEqualTo("semaine. le document");
    }
}
