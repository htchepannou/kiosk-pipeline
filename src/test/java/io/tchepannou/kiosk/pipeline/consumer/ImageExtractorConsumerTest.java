package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Image;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ImageRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.image.ImageExtractor;
import io.tchepannou.kiosk.pipeline.support.HtmlHelper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import static io.tchepannou.kiosk.pipeline.Fixtures.createS3InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImageExtractorConsumerTest {
    @Mock
    AmazonS3 s3;

    @Mock
    AmazonSNS sns;

    @Mock
    LinkRepository linkRepository;

    @Mock
    ImageRepository imageRepository;

    @Mock
    ImageExtractor extractor;

    @Mock
    HttpService http;

    @InjectMocks
    ImageExtractorConsumer consumer;

    @Before
    public void setUp() {
        consumer.setInputQueue("input-queue");
        consumer.setOutputTopic("output-topic");
        consumer.setS3Bucket("bucket");
        consumer.setS3Key("dev/img");
        consumer.setS3KeyHtml("dev/html");

        when(imageRepository.findByLinkByTypeByUrl(any(Link.class), anyInt(), anyString())).thenReturn(Collections.emptyList());
    }

    @Test
    public void shouldDownloadImage() throws Exception {
        // Given
        final String s3Key = "dev/html/2010/10/11/test.html";
        final Link link = new Link();
        link.setId(123);
        link.setS3Key(s3Key);
        when(linkRepository.findOne(123L)).thenReturn(link);

        final String html = IOUtils.toString(getClass().getResource("/image/article.html"));
        final S3ObjectInputStream in = createS3InputStream(html);
        final S3Object obj = createS3Object("bucket", s3Key);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", s3Key)).thenReturn(obj);

        when(extractor.extract(anyString())).thenReturn("http://camfoot.com/IMG/arton25520.jpg");

        doAnswer(get("/image/jordan.jpg", "image/jpeg")).when(http).get(any(), any());

        doAnswer(saveImage(567)).when(imageRepository).save(any(Image.class));

        // Then
        consumer.consumeMessage("123");

        // Then
        ArgumentCaptor<ObjectMetadata> meta = ArgumentCaptor.forClass(ObjectMetadata.class);
        verify(s3).putObject(
                eq("bucket"),
                eq("dev/img/2010/10/11/test.jpg"),
                any(InputStream.class),
                meta.capture()
        );
        assertThat(meta.getValue().getContentType()).isEqualTo("image/jpeg");
        assertThat(meta.getValue().getCacheControl()).isEqualTo(HtmlHelper.CACHE_CONTROL_CACHE_FOR_30_DAYS);
        assertThat(meta.getValue().getContentLength()).isGreaterThan(0);

        final ArgumentCaptor<Image> img = ArgumentCaptor.forClass(Image.class);
        verify(imageRepository).save(img.capture());
        assertThat(img.getValue().getLink()).isEqualTo(link);
        assertThat(img.getValue().getS3Key()).isEqualTo("dev/img/2010/10/11/test.jpg");
        assertThat(img.getValue().getType()).isEqualTo(Image.TYPE_ORIGINAL);
        assertThat(img.getValue().getUrl()).isEqualTo("http://camfoot.com/IMG/arton25520.jpg");
        assertThat(img.getValue().getContentType()).isEqualTo("image/jpeg");
        assertThat(img.getValue().getContentLength()).isEqualTo(4490750L);
        assertThat(img.getValue().getWidth()).isEqualTo(2400);
        assertThat(img.getValue().getHeight()).isEqualTo(3000);

        verify(sns).publish("output-topic", "567");
    }

    @Test
    public void shouldNotDownloadExistingImage() throws Exception {
        // Given
        final String s3Key = "dev/html/2010/10/11/test.html";
        final Link link = new Link();
        link.setId(123);
        link.setS3Key(s3Key);
        when(linkRepository.findOne(123L)).thenReturn(link);

        final String html = IOUtils.toString(getClass().getResource("/image/article.html"));
        final S3ObjectInputStream in = createS3InputStream(html);
        final S3Object obj = createS3Object("bucket", s3Key);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", s3Key)).thenReturn(obj);

        when(extractor.extract(anyString())).thenReturn("http://camfoot.com/IMG/arton25520.jpg");

        doAnswer(get("/image/jordan.jpg", "image/jpeg")).when(http).get(any(), any());

        final Image img = new Image();
        img.setId(567);
        when(imageRepository.findByLinkByTypeByUrl(link, Image.TYPE_ORIGINAL, "http://camfoot.com/IMG/arton25520.jpg")).thenReturn(Collections.singletonList(img));


        // Then
        consumer.consumeMessage("123");

        // Then
        verify(s3, never()).putObject(
                anyString(),
                anyString(),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );

        verify(imageRepository, never()).save(any(Image.class));
        verify(sns, never()).publish(anyString(), anyString());
    }

    @Test
    public void shouldNormalizeImageName() throws Exception {
        // Given
        final String s3Key = "dev/html/2010/10/11/aeoi1f.html";
        final Link link = new Link();
        link.setId(123);
        link.setS3Key(s3Key);
        when(linkRepository.findOne(123L)).thenReturn(link);

        final String html = IOUtils.toString(getClass().getResource("/image/article.html"));
        final S3ObjectInputStream in = createS3InputStream(html);
        final S3Object obj = createS3Object("bucket", s3Key);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", s3Key)).thenReturn(obj);

        when(extractor.extract(anyString())).thenReturn("http://camfoot.com/IMG/arton25520.jpg?124354");

        doAnswer(get("/image/jordan.jpg", "image/jpeg")).when(http).get(any(), any());

        doAnswer(saveImage(567)).when(imageRepository).save(any(Image.class));

        // Then
        consumer.consumeMessage("123");

        // Then
        verify(s3).putObject(
                eq("bucket"),
                eq("dev/img/2010/10/11/aeoi1f.jpg"),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );

        final ArgumentCaptor<Image> img = ArgumentCaptor.forClass(Image.class);
        verify(imageRepository).save(img.capture());
        assertThat(img.getValue().getLink()).isEqualTo(link);
        assertThat(img.getValue().getS3Key()).isEqualTo("dev/img/2010/10/11/aeoi1f.jpg");
        assertThat(img.getValue().getType()).isEqualTo(Image.TYPE_ORIGINAL);
        assertThat(img.getValue().getUrl()).isEqualTo("http://camfoot.com/IMG/arton25520.jpg?124354");
        assertThat(img.getValue().getContentType()).isEqualTo("image/jpeg");
        assertThat(img.getValue().getContentLength()).isEqualTo(4490750L);
        assertThat(img.getValue().getWidth()).isEqualTo(2400);
        assertThat(img.getValue().getHeight()).isEqualTo(3000);

        verify(sns).publish("output-topic", "567");
    }

    @Test
    public void shouldRejectCorruptedImage() throws Exception {
        // Given
        final String s3Key = "dev/html/2010/10/11/test.html";
        final Link link = new Link();
        link.setId(123);
        link.setS3Key(s3Key);
        when(linkRepository.findOne(123L)).thenReturn(link);

        final String html = IOUtils.toString(getClass().getResource("/image/article.html"));
        final S3ObjectInputStream in = createS3InputStream(html);
        final S3Object obj = createS3Object("bucket", s3Key);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", s3Key)).thenReturn(obj);

        when(extractor.extract(anyString())).thenReturn("http://camfoot.com/IMG/arton25520.jpg");

        doAnswer(get("/image/article.html", "image/jpeg")).when(http).get(any(), any());

        // Then
        consumer.consumeMessage("123");

        // Then
        verify(s3, never()).putObject(
                anyString(),
                anyString(),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );

        verify(imageRepository, never()).save(any(Image.class));
        verify(sns, never()).publish(anyString(), anyString());
    }

    @Test
    public void shouldRejectNonImageContentType() throws Exception {
        // Given
        final String s3Key = "dev/html/2010/10/11/test.html";
        final Link link = new Link();
        link.setId(123);
        link.setS3Key(s3Key);
        when(linkRepository.findOne(123L)).thenReturn(link);

        final String html = IOUtils.toString(getClass().getResource("/image/article.html"));
        final S3ObjectInputStream in = createS3InputStream(html);
        final S3Object obj = createS3Object("bucket", s3Key);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject("bucket", s3Key)).thenReturn(obj);

        when(extractor.extract(anyString())).thenReturn("http://camfoot.com/IMG/arton25520.jpg");

        doAnswer(get("/image/article.html", "text/html")).when(http).get(any(), any());

        // Then
        consumer.consumeMessage("123");

        // Then
        verify(s3, never()).putObject(
                anyString(),
                anyString(),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );

        verify(imageRepository, never()).save(any(Image.class));
        verify(sns, never()).publish(anyString(), anyString());
    }

    private S3Object createS3Object(final String bucket, final String key) throws Exception {
        final S3Object obj = mock(S3Object.class);
        when(obj.getBucketName()).thenReturn(bucket);
        when(obj.getKey()).thenReturn(key);

        return obj;
    }

    private Answer get(final String path, final String contentType) {
        return (inv) -> {
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            final InputStream in = getClass().getResourceAsStream(path);
            IOUtils.copy(in, out);
            return contentType;
        };
    }

    private Answer saveImage(final long id) {
        return (inv) -> {
            final Image img = (Image) inv.getArguments()[0];
            img.setId(id);
            return null;
        };
    }
}
