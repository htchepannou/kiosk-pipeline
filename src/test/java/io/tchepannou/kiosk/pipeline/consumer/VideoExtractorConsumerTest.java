package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.Video;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.VideoRepository;
import io.tchepannou.kiosk.pipeline.service.video.VideoExtractor;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;

import static io.tchepannou.kiosk.pipeline.Fixtures.createS3InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class VideoExtractorConsumerTest {
    @Mock
    AmazonS3 s3;

    @Mock
    LinkRepository linkRepository;

    @Mock
    VideoRepository videoRepository;

    @Mock
    VideoExtractor extractor;

    @InjectMocks
    VideoExtractorConsumer consumer;

    @Before
    public void setUp() {
        consumer.setInputQueue("input-queue");
        consumer.setS3Bucket("bucket");
    }


    @Test
    public void shouldExtractVideo() throws Exception {
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

        when(extractor.extract(anyString())).thenReturn(Arrays.asList("http://youtu.be/XOcCOBe8PTc"));

        doAnswer(saveVideo(567)).when(videoRepository).save(any(Video.class));

        // Then
        consumer.consumeMessage("123");

        // Then
        final ArgumentCaptor<Video> img = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(img.capture());
        assertThat(img.getValue().getLink()).isEqualTo(link);
        assertThat(img.getValue().getEmbedUrl()).isEqualTo("http://youtu.be/XOcCOBe8PTc");
    }


    @Test
    public void shouldRejectMessagesWithNoVideo() throws Exception {
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

        when(extractor.extract(anyString())).thenReturn(new ArrayList<>());

        // Then
        consumer.consumeMessage("123");

        // Then
        verify(videoRepository, never()).save(any(Video.class));
    }

    @Test
    public void shouldRejectVideoAlreadyDownloaded() throws Exception {
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

        when(extractor.extract(anyString())).thenReturn(Arrays.asList("http://youtu.be/XOcCOBe8PTc"));

        when(videoRepository.findByEmbedUrl("http://youtu.be/XOcCOBe8PTc")).thenReturn(Arrays.asList(new Video()));

        // Then
        consumer.consumeMessage("123");

        // Then
        verify(videoRepository, never()).save(any(Video.class));
    }

    private S3Object createS3Object(final String bucket, final String key) throws Exception {
        final S3Object obj = mock(S3Object.class);
        when(obj.getBucketName()).thenReturn(bucket);
        when(obj.getKey()).thenReturn(key);

        return obj;
    }

    private Answer saveVideo(final long id) {
        return (inv) -> {
            final Video img = (Video) inv.getArguments()[0];
            img.setId(id);
            return null;
        };
    }

}
