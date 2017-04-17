package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleVideoFilterTest {
    TitleFilter filter = new TitleVideoFilter();
    Feed feed = new Feed();

    @Test
    public void shouldFilterTitle() throws Exception {
        assertThat(filter.filter("Title (video)", feed)).isEqualTo("Title");
        assertThat(filter.filter("Title ( video )", feed)).isEqualTo("Title");
        assertThat(filter.filter("Title [video]", feed)).isEqualTo("Title");
        assertThat(filter.filter("Title [ Video ]", feed)).isEqualTo("Title");
        assertThat(filter.filter("[►] Title", feed)).isEqualTo("Title");
        assertThat(filter.filter("Title [ Vidéo ]", feed)).isEqualTo("Title");
    }
}
