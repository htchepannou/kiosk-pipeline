package io.tchepannou.kiosk.pipeline.step.content.filter;

import io.tchepannou.kiosk.pipeline.step.content.filter.TrimFilter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TrimFilterTest {

    @Test
    public void testFilter() throws Exception {
        final TrimFilter filter = new TrimFilter();

        assertThat(filter.filter("|hello")).isEqualTo("hello");
    }
}
