package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.Fixtures;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.title.TitleSanitizer;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArticleMetadataConsumerTest {

    @Mock
    AmazonSQS sqs;

    @Mock
    AmazonS3 s3;

    @Mock
    ArticleRepository articleRepository;

    @Mock
    LinkRepository linkRepository;

    @Mock
    TitleSanitizer titleSanitizer;

    @InjectMocks
    ArticleMetadataConsumer consumer;

    @Before
    public void setUp() {
        consumer.setInputQueue("input-queue");
        consumer.setOutputQueue("output-queue");
        consumer.setS3Bucket("bucket");
    }

    @Test
    public void testConsume() throws Exception {
        // Given
        final Link link = createLink();
        when(linkRepository.findOne(123L)).thenReturn(link);

        final InputStream xin = getClass().getResourceAsStream("/meta/article.html");
        final String html = IOUtils.toString(xin);
        final S3ObjectInputStream in = Fixtures.createS3InputStream(html);
        final S3Object obj = mock(S3Object.class);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", link.getS3Key())).thenReturn(obj);

        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        when(titleSanitizer.filter(any())).thenReturn("This is the sanitized title");

        doAnswer(saveArticle(567)).when(articleRepository).save(any(Article.class));

        // Then
        consumer.consumeMessage("123");

        // Then
        ArgumentCaptor<Article> article = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(article.capture());

        assertThat(article.getValue().getTitle()).isEqualTo("Rigobert Song : « Je suis vraiment revenu de très loin »");
        assertThat(article.getValue().getDisplayTitle()).isEqualTo("This is the sanitized title");
        assertThat(article.getValue().getSummary()).isEqualTo("Et soudain, Rigobert Song apparaît dans l’embrasure de la porte. Quelques kilos en moins, des cheveu...");
        assertThat(fmt.format(article.getValue().getPublishedDate())).startsWith("2016-12-29");
        assertThat(article.getValue().getLink()).isEqualTo(link);

        verify(sqs).sendMessage("output-queue", "567");
    }

    @Test
    public void shouldExtractTitleFromSparkCameroon() throws Exception {
        final Document doc = loadDocument("/meta/sparkcameroon.html");
        assertThat(consumer.extractTitle(doc)).isEqualTo("Les motos-taxis en ordre de bataille contre le sida");
    }

    @Test
    public void shouldExtractPublishedDateFromSparkCameroon() throws Exception {
        final Document doc = loadDocument("/meta/sparkcameroon.html");
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        assertThat(fmt.format(consumer.extractPublishedDate(doc))).isEqualTo("2016-12-04");
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


    private Answer saveArticle(final long id) {
        return (inv) -> {
            final Article img = (Article) inv.getArguments()[0];
            img.setId(id);
            return null;
        };
    }

}
