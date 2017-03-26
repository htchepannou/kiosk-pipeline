package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleCountryFilterTest {
    TitleFilter filter = new TitleCountryFilter();
    Feed article = new Feed ();

    @Test
    public void testFilter() throws Exception {
        assertThat(filter.filter("CAMEROUN :: Eséka : Ville cruelle, ville maudite :: CAMEROON", article))
                .isEqualTo("Eséka : Ville cruelle, ville maudite");

        assertThat(filter.filter("Eséka : Ville cruelle, ville maudite :: CAMEROON", article))
                .isEqualTo("Eséka : Ville cruelle, ville maudite");

        assertThat(filter.filter("CAMEROUN :: Eséka : Ville cruelle, ville maudite", article))
                .isEqualTo("Eséka : Ville cruelle, ville maudite");

        assertThat(filter.filter(" Eséka : Ville cruelle, ville maudite", article))
                .isEqualTo("Eséka : Ville cruelle, ville maudite");
    }
}
