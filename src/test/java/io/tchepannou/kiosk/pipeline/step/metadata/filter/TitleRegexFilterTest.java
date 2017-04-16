package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleRegexFilterTest {
    TitleFilter filter = new TitleRegexFilter();

    @Test
    public void shouldFilterOutPrefixAndSuffix() throws Exception {
        // Given
        final Feed feed = new Feed();
        feed.setDisplayTitleRegex(".+::(.+)::.+");

        // When/Then
        assertThat(filter.filter("CAMEROUN :: Eséka : Ville cruelle, ville maudite :: CAMEROON", feed))
                .isEqualTo("Eséka : Ville cruelle, ville maudite");
    }

    @Test
    public void shouldFilterOutWhenRegexDontMatch() throws Exception {
        // Given
        final Feed feed = new Feed();
        feed.setDisplayTitleRegex(".+::(.+)::.+");

        // When
        final String result = filter.filter("Eséka : Ville cruelle, ville maudite", feed);

        // Then
        assertThat(result).isEqualTo("Eséka : Ville cruelle, ville maudite");
    }

    @Test
    public void shouldNotFilterOutWhenNoRegexPrefix() throws Exception {
        // When
        final String result = filter.filter("CAMEROUN :: Eséka : Ville cruelle, ville maudite", new Feed());

        // Then
        assertThat(result).isEqualTo("CAMEROUN :: Eséka : Ville cruelle, ville maudite");
    }
}
