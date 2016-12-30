package io.tchepannou.kiosk.pipeline.persistence.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FeedTest {
    @Test
    public void urlShouldBeLowerCase(){
        Feed feed = new Feed();
        feed.setUrl("http://GooGle.com");

        assertThat(feed.getUrl()).isEqualTo("http://google.com");
    }

    @Test
    public void urlShouldNotEndsWithSlash(){
        Feed feed = new Feed();
        feed.setUrl("http://google.com/");

        assertThat(feed.getUrl()).isEqualTo("http://google.com");
    }
}
