package io.tchepannou.kiosk.pipeline.service.image;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ImageExtractorTest {
    @InjectMocks
    ImageExtractor extractor;

    @Test
    public void shouldExtractNoImage() throws Exception {
        final String html = "<html></html>";

        assertThat(extractor.extract(html)).isNull();
    }

    @Test
    public void shouldExtractImageFromOGImage() throws Exception {
        final String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"fr\" lang=\"fr\" dir=\"ltr\">\n"
                + "<head>\n"
                + "    <base href=\"http://camfoot.com/\"/>\n"
                + "    <title>Rigobert Song : « Je suis vraiment revenu de très loin » - Camfoot.com</title>\n"
                + "    <meta name=\"description\"\n"
                + "          content=\" Et soudain, Rigobert Song appara&#238;t dans l&#039;embrasure de la porte. Quelques kilos en moins, des cheveux coup&#233;s courts, mais un sourire toujours aussi (...) \"/>\n"
                + "\n"
                + "    <link rel=\"canonical\" href=\"http://camfoot.com/actualites/rigobert-song-je-suis-vraiment-revenu-de-tres-loin,25520.html\"/>\n"
                + "\n"
                + "    <meta property=\"og:site_name\" content=\"Camfoot.com\"/>\n"
                + "    <meta property=\"og:locale\" content=\"fr_FR\"/>\n"
                + "\n"
                + "\n"
                + "    <meta property=\"og:url\" content=\"http://camfoot.com/actualites/rigobert-song-je-suis-vraiment-revenu-de-tres-loin,25520.html\"/>\n"
                + "    <meta property=\"og:type\" content=\"article\"/>\n"
                + "    <meta property=\"og:title\" content=\"Rigobert Song : « Je suis vraiment revenu de très loin »\"/>\n"
                + "    <meta property=\"og:description\"\n"
                + "          content=\"Et soudain, Rigobert Song apparaît dans l’embrasure de la porte. Quelques kilos en moins, des cheveux coupés courts, mais un sourire toujours aussi éclatant, communicatif. L’ancien capitaine du Cameroun, victime d’un accident vasculaire cérébral (AVC) avec rupture d’anévrisme (1), le 1er octobre dernier à Yaoundé, rejoint la salle de rééducation de l’hôpital parisien de la Pitié-Salpêtrière. Il marche à son rythme, ne présente quasiment aucune séquelle, si ce n’est trois orteils du pied droit encore faibles, (...)\"/>\n"
                + "    <meta property=\"og:image\"\n"
                + "          content=\"http://camfoot.com/IMG/arton25520.jpg?1482594254\"/>\n"
                + "\n"
                + "</head>\n"
                + "<body>\n"
                + "\n"
                + "</body>\n"
                + "</html>\n";

        assertThat(extractor.extract(html)).isEqualTo("http://camfoot.com/IMG/arton25520.jpg?1482594254");
    }

    @Test
    public void shouldExtractImageFromTweeterImage() throws Exception {
        final String html = "<html>"
                + "<head>"
                + "<meta property=\"twitter:image\" content=\"http://camfoot.com/IMG/arton25520.jpg?1482594254\"/>"
                + "</head>"
                + "</html>";

        assertThat(extractor.extract(html)).isEqualTo("http://camfoot.com/IMG/arton25520.jpg?1482594254");
    }

    @Test
    public void shouldExtractImageFromTheSpark() throws Exception {
        final String html = IOUtils.toString(getClass().getResourceAsStream("/image/sparkcameroon.html"));

        assertThat(extractor.extract(html)).isEqualTo("http://www.sparkcameroun.com/wp-content/uploads/2015/12/81440056.jpg");
    }
}
