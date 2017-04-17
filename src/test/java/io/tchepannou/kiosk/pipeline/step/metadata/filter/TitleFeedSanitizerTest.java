package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.persistence.domain.Link;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleFeedSanitizerTest {

    Feed feed;
    Link link;

    TitleFeedFilter sanitizer = new TitleFeedFilter();

    @Before
    public void setUp() {
        feed = new Feed();
        feed.setName("Je Wanda Magazine");

        link = new Link();
        link.setFeed(feed);
    }

    @Test
    public void shouldRemoveNeedNameFromTitle() throws Exception {
        final String title = "This is a sample of title - " + feed.getName();

        assertThat(sanitizer.filter(title, feed)).isEqualTo("This is a sample of title - ");
    }

    @Test
    public void shouldRemoveNeedNameFromTitleCaseInsensitive() throws Exception {
        final String title = "This is a sample of title - " + feed.getName().toLowerCase();

        assertThat(sanitizer.filter(title, feed)).isEqualTo("This is a sample of title - ");
    }


    @Test
    public void shouldNotChangeTitleWhenFeedNotFound() throws Exception {
        final String title = "This is a sample of title";

        assertThat(sanitizer.filter(title, feed)).isEqualTo("This is a sample of title");
    }
}
