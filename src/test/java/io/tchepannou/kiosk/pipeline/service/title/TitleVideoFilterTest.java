package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleVideoFilterTest {
    TitleFilter filter = new TitleVideoFilter();
    Article article = new Article ();

    @Test
    public void shouldFilterTitle() throws Exception {
        assertThat(filter.filter("Title (video)", article)).isEqualTo("Title");
        assertThat(filter.filter("Title ( video )", article)).isEqualTo("Title");
        assertThat(filter.filter("Title [video]", article)).isEqualTo("Title");
        assertThat(filter.filter("Title [ Video ]", article)).isEqualTo("Title");
        assertThat(filter.filter("[►] Title", article)).isEqualTo("Title");
        assertThat(filter.filter("Title [ Vidéo ]", article)).isEqualTo("Title");
    }
}
