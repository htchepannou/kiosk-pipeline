package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.content.ContentExtractor;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.time.Clock;
import java.util.Date;

import static io.tchepannou.kiosk.pipeline.Fixtures.createS3InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentExtractorConsumerTest {
    @Mock
    AmazonS3 s3;

    @Mock
    AmazonSQS sqs;

    @Mock
    LinkRepository linkRepository;

    @Mock
    ContentExtractor extractor;

    @Mock
    ArticleRepository articleRepository;

    @Mock
    Clock clock;

    @InjectMocks
    ContentExtractorConsumer consumer;

    @Before
    public void setUp() {
        consumer.setS3Bucket("bucket");
        consumer.setS3Key("dev/content");
        consumer.setS3KeyHtml("dev/html");
        consumer.setOutputQueue("output-queue");
    }

    @Test
    public void shouldConsume() throws Exception {
        // Given
        final String s3Key = "dev/html/2010/10/11/test.html";
        final Link link = new Link ();
        link.setId(123);
        link.setS3Key(s3Key);
        when(linkRepository.findOne(123L)).thenReturn(link);

        final S3Object obj = createS3Object("bucket", s3Key);
        final S3ObjectInputStream in = createS3InputStream("hello world");
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", s3Key)).thenReturn(obj);

        final String html = IOUtils.toString(getClass().getResourceAsStream("/extractor/content_default_filtered.html"));
        when(extractor.extract("hello world")).thenReturn(html);

        when(clock.millis()).thenReturn(1111L);

        // When
        consumer.consumeMessage("123");

        // Then
        verify(s3).putObject(
                eq("bucket"),
                eq("dev/content/2010/10/11/test.html"),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );

        ArgumentCaptor<Article> article = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(article.capture());
        assertThat(article.getValue().getLink()).isEqualTo(link);
        assertThat(article.getValue().getS3Key()).isEqualTo("dev/content/2010/10/11/test.html");
        assertThat(article.getValue().getPublishedDate()).isEqualTo(new Date(1111L));
        assertThat(article.getValue().getContentLength()).isEqualTo(17116);
        assertThat(article.getValue().getSummary()).isEqualTo("WACO, Texas — Rico Gathers snared 11 rebounds and swatted four shots, but the pro scout watching Bay...");

        verify(sqs).sendMessage(eq("output-queue"), anyString());
    }

    private S3Object createS3Object(final String bucket, final String key) throws Exception {
        final S3Object obj = mock(S3Object.class);
        when(obj.getBucketName()).thenReturn(bucket);
        when(obj.getKey()).thenReturn(key);

        return obj;
    }
}
