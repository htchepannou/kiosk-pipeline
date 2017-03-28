package io.tchepannou.kiosk.pipeline.step.content.filter;

import io.tchepannou.kiosk.pipeline.step.content.Filter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnchorFilterTest {
    Filter<String> filter = new AnchorFilter();

    @Test
    public void shouldFilterHttpUrl() throws Exception {
        final String html = "<a href='http://www.google.ca'>Email</a><p>hello</p>";

        assertThat(filter.filter(html)).isEqualTo("<html>\n"
                + " <head></head>\n"
                + " <body>\n"
                + "  <a href=\"#\">Email</a>\n"
                + "  <p>hello</p>\n"
                + " </body>\n"
                + "</html>");
    }

    @Test
    public void shouldFilterHttpsUrl() throws Exception {
        final String html = "<a href='https://www.google.ca'>Email</a><p>hello</p>";

        assertThat(filter.filter(html)).isEqualTo("<html>\n"
                + " <head></head>\n"
                + " <body>\n"
                + "  <a href=\"#\">Email</a>\n"
                + "  <p>hello</p>\n"
                + " </body>\n"
                + "</html>");
    }

    @Test
    public void shouldFilterUrlWithEmails() throws Exception {
        final String html = "<a href='mailto:foo@gmail.com'>Email</a><p>hello</p>";

        assertThat(filter.filter(html)).isEqualTo("<html>\n"
                + " <head></head>\n"
                + " <body>\n"
                + "  <a href=\"#\">Email</a>\n"
                + "  <p>hello</p>\n"
                + " </body>\n"
                + "</html>");
    }

    @Test
    public void shouldFilterUrlWithDash() throws Exception {
        final String html = "<a href='http://www.google.ca#comments'>Email</a><p>hello</p>";

        assertThat(filter.filter(html)).isEqualTo("<html>\n"
                + " <head></head>\n"
                + " <body>\n"
                + "  <a href=\"#\">Email</a>\n"
                + "  <p>hello</p>\n"
                + " </body>\n"
                + "</html>");
    }
}
