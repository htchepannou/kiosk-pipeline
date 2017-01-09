package io.tchepannou.kiosk.pipeline.service.content;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnchorFilterTest {
    Filter<String> filter = new AnchorFilter();

    @Test
    public void shouldOpenHttpUrl() throws Exception {
        final String html = "<a href='http://www.google.ca'>Email</a><p>hello</p>";

        assertThat(filter.filter(html)).isEqualTo("<html>\n"
                + " <head></head>\n"
                + " <body>\n"
                + "  <a href=\"#\" onclick=\"navigate('http://www.google.ca')\" class=\"kiosk-link\">Email</a>\n"
                + "  <p>hello</p>\n"
                + " </body>\n"
                + "</html>");
    }

    @Test
    public void shouldOpenHttpsUrl() throws Exception {
        final String html = "<a href='https://www.google.ca'>Email</a><p>hello</p>";

        assertThat(filter.filter(html)).isEqualTo("<html>\n"
                + " <head></head>\n"
                + " <body>\n"
                + "  <a href=\"#\" onclick=\"navigate('https://www.google.ca')\" class=\"kiosk-link\">Email</a>\n"
                + "  <p>hello</p>\n"
                + " </body>\n"
                + "</html>");
    }

    @Test
    public void shouldSanitizeAnchorWithEmails() throws Exception {
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
    public void shouldSanitizeAnchorWithDash() throws Exception {
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
