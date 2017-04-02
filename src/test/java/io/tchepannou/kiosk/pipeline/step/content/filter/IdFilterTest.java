package io.tchepannou.kiosk.pipeline.step.content.filter;

import io.tchepannou.kiosk.pipeline.step.content.filter.IdFilter;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IdFilterTest {

    @Test
    public void testFilter() throws Exception {
        // Given
        final String html = IOUtils.toString(getClass().getResourceAsStream("/extractor/id.html"));

        // When
        final String result = new IdFilter().filter(html);
//        System.out.println(result);

        // Then
        final String expected = IOUtils.toString(getClass().getResourceAsStream("/extractor/id_filtered.html"));
        assertThat(expected).isNotNull();
    }
}
