package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.tchepannou.kiosk.pipeline.Fixtures;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataExtractorConsumerTest {

    @Mock
    AmazonS3 s3;

    @Mock
    ArticleRepository articleRepository;

    @InjectMocks
    MetadataExtractorConsumer consumer;

    @Before
    public void setUp() {
        consumer.setInputQueue("input-queue");
        consumer.setS3Bucket("bucket");
    }

    @Test
    public void testConsume() throws Exception {
        // Given
        final Link link = createLink();
        final Article article = createArticle(link);
        when(articleRepository.findOne(123L)).thenReturn(article);

        final InputStream xin = getClass().getResourceAsStream("/meta/article.html");
        final String html = IOUtils.toString(xin);
        final S3ObjectInputStream in = Fixtures.createS3InputStream(html);
        final S3Object obj = mock(S3Object.class);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", link.getS3Key())).thenReturn(obj);

        // Then
        consumer.consume("123");

        // Then
        assertThat(article.getTitle()).isEqualTo("Rigobert Song : « Je suis vraiment revenu de très loin »");
        assertThat(article.getSummary()).isEqualTo(
                "Et soudain, Rigobert Song apparaît dans l’embrasure de la porte. Quelques kilos en moins, des cheveux coupés courts, mais un sourire toujours aussi éclatant, communicatif. L’ancien capitaine du Cameroun, victime d’un accident vasculaire cérébral (AVC) avec rupture d’anévrisme (1), le 1er octobre dernier à Yaoundé, rejoint la salle de rééducation de l’hôpital parisien de la Pitié-Salpêtrière. Il marche à son rythme, ne présente quasiment aucune séquelle, si ce n’est trois orteils du pied droit encore faibles, (...)");
        verify(articleRepository).save(article);
    }

    @Test
    public void shouldExtractTitleFromSparkCameroon() throws Exception {
        final Document doc = loadDocument("/meta/sparkcameroon.html");
        assertThat(consumer.extractTitle(doc)).isEqualTo("Les motos-taxis en ordre de bataille contre le sida");
    }

    private Document loadDocument(final String path) throws Exception {
        final String html = IOUtils.toString(getClass().getResourceAsStream(path));
        return Jsoup.parse(html);
    }

    private Link createLink() {
        final Link link = new Link();
        link.setS3Key("dev/html/2011/01/01/foo.html");
        return link;
    }

    private Article createArticle(final Link link) {
        final Article article = new Article();
        article.setLink(link);
        return article;
    }
}
