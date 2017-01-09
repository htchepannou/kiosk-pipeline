package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import org.junit.Test;

import static io.tchepannou.kiosk.pipeline.Fixtures.createArticle;
import static org.assertj.core.api.Assertions.assertThat;

public class TitleRegexFilterTest {
    TitleFilter filter = new TitleRegexFilter();

    @Test
    public void shouldFilterOutPrefixAndSuffix() throws Exception {
        // Given
        final Feed feed = new Feed();
        feed.setDisplayTitleRegex(".+::(.+)::.+");

        final Article article = createArticle();
        article.getLink().setFeed(feed);

        // When/Then
        assertThat(filter.filter("CAMEROUN :: Eséka : Ville cruelle, ville maudite :: CAMEROON", article))
                .isEqualTo("Eséka : Ville cruelle, ville maudite");
    }

    @Test
    public void shouldFilterOutWhenRegexDontMatch() throws Exception {
        // Given
        final Feed feed = new Feed();
        feed.setDisplayTitleRegex(".+::(.+)::.+");

        final Article article = createArticle();
        article.getLink().setFeed(feed);

        // When
        final String result = filter.filter("Eséka : Ville cruelle, ville maudite", article);

        // Then
        assertThat(result).isEqualTo("Eséka : Ville cruelle, ville maudite");
    }

    @Test
    public void shouldNotFilterOutWhenNoRegexPrefix() throws Exception {
        // Given
        final Article article = createArticle();

        // When
        final String result = filter.filter("CAMEROUN :: Eséka : Ville cruelle, ville maudite", article);

        // Then
        assertThat(result).isEqualTo("CAMEROUN :: Eséka : Ville cruelle, ville maudite");
    }
}
