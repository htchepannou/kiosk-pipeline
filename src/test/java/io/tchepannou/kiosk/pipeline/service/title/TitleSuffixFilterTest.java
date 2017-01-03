package io.tchepannou.kiosk.pipeline.service.title;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleSuffixFilterTest {
    TitleSuffixFilter sanitizer = new TitleSuffixFilter();

    @Test
    public void shouldTrimSpace() {
        final String title = "This is a title ";

        assertThat(sanitizer.filter(title, null)).isEqualToIgnoringCase("This is a title");
    }

    @Test
    public void shouldTrimPipe() {
        final String title = "This is a title |";

        assertThat(sanitizer.filter(title, null)).isEqualToIgnoringCase("This is a title");
    }

    @Test
    public void shouldTrimDash() {
        final String title = "This is a title-";

        assertThat(sanitizer.filter(title, null)).isEqualToIgnoringCase("This is a title");
    }

    @Test
    public void shouldTrimSemiColon() {
        final String title = "This is a title :";

        assertThat(sanitizer.filter(title, null)).isEqualToIgnoringCase("This is a title");
    }
}
