package io.tchepannou.kiosk.pipeline.service;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlBlacklistServiceTest {
    UrlBlacklistService service;

    @Test
    public void testIsBlacklisted() throws Exception {
        // Given
        service = new UrlBlacklistService();
        service.getUrls().add("*/wp-login.php*");
        service.getUrls().add("*/feed/rss");

        // Then
        assertThat(service.contains(null)).isTrue();
        assertThat(service.contains("")).isTrue();
        assertThat(service.contains("http://www.sparkcameroun.com/wp-login.php?action=register")).isTrue();
        assertThat(service.contains("http://www.sparkcameroun.com/wp-login.php")).isTrue();
        assertThat(service.contains("http://www.camer24.de/feed/rss/")).isTrue();

        assertThat(service.contains("http://www.sparkcameroun.com")).isFalse();
    }
}
