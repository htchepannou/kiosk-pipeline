package io.tchepannou.kiosk.core.nlp.filter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HyphenFilterTest {
    TextFilter filter = new HyphenFilter();

    @Test
    public void testFilter() throws Exception {
        assertThat(filter.filter("Jean-Paul Belmondon")).isEqualTo("Jean Paul Belmondon");
    }

}
