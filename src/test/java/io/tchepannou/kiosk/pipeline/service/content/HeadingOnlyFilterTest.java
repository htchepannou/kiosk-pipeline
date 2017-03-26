package io.tchepannou.kiosk.pipeline.service.content;

import io.tchepannou.kiosk.pipeline.step.content.Filter;
import io.tchepannou.kiosk.pipeline.step.content.filter.HeadingOnlyFilter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HeadingOnlyFilterTest {
    Filter<String> filter = new HeadingOnlyFilter();

    @Test
    public void shouldReturnEmptyWhenDocumentHasOnlyHeading() throws Exception {
        assertThat(filter.filter("<h1>hello</h1>")).isEmpty();
        assertThat(filter.filter("<h2>hello</h2>")).isEmpty();
        assertThat(filter.filter("<h3>hello</h3>")).isEmpty();
        assertThat(filter.filter("<h4>hello</h4>")).isEmpty();
        assertThat(filter.filter("<h5>hello</h5>")).isEmpty();
        assertThat(filter.filter("<h6>hello</h6>")).isEmpty();
        assertThat(filter.filter("<html><body><h6>hello</h6></body></html>")).isEmpty();
    }

    @Test
    public void shouldReturnDocumentWhenDocumentHaveHeadingAndOtherElement() throws Exception {
        assertThat(filter.filter("<h1>Hello</h1><p>hello</p>")).isEqualTo("<h1>Hello</h1><p>hello</p>");
    }

    @Test
    public void shouldReturnDocumentWhenDocumentDoesntHaveHeading() throws Exception {
        assertThat(filter.filter("<p>hello</p>")).isEqualTo("<p>hello</p>");
    }
}
