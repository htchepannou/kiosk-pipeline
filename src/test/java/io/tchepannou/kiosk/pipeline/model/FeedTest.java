package io.tchepannou.kiosk.pipeline.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FeedTest {

    @Test
    public void testUrlMatchesWithoutPath() throws Exception {
        Feed feed = new Feed();
        feed.setUrl("http://www.google.com");
        feed.setPath(null);

        assertThat(feed.urlMatches("http://www.google.com")).isTrue();
        assertThat(feed.urlMatches("http://www.google.com/test.com")).isTrue();

        assertThat(feed.urlMatches("http://www.google.com_file.txt")).isFalse();
        assertThat(feed.urlMatches("http://www.google.ca")).isFalse();
        assertThat(feed.urlMatches("http://www.google.ca/test.com")).isFalse();
    }

    @Test
    public void testUrlMatchesWithPath() throws Exception {
        Feed feed = new Feed();
        feed.setUrl("http://www.google.com");
        feed.setPath("/article/*.html");

        assertThat(feed.urlMatches("http://www.google.com/article/test_123290.html")).isTrue();

        assertThat(feed.urlMatches("http://www.google.com")).isFalse();
        assertThat(feed.urlMatches("http://www.google.com/article")).isFalse();
        assertThat(feed.urlMatches("http://www.google.com/article/test_123290.csv")).isFalse();

        assertThat(feed.urlMatches("http://www.google.ca")).isFalse();
        assertThat(feed.urlMatches("http://www.google.ca/article/test_123290.html")).isFalse();
    }
}
