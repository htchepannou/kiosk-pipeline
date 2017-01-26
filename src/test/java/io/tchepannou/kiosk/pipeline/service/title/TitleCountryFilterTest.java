package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleCountryFilterTest {
    TitleFilter filter = new TitleCountryFilter();
    Article article = new Article ();

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
