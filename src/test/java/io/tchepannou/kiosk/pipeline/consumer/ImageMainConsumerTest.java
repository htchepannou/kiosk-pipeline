package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.tchepannou.kiosk.pipeline.persistence.domain.Image;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ImageRepository;
import io.tchepannou.kiosk.pipeline.service.image.ImageProcessorService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.io.OutputStream;

import static io.tchepannou.kiosk.pipeline.Fixtures.createS3InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImageMainConsumerTest {
    @Mock
    AmazonS3 s3;

    @Mock
    ImageRepository imageRepository;

    @Mock
    ImageProcessorService imageProcessorService;

    @InjectMocks
    ImageMainConsumer consumer;

    @Before
    public void setUp() {
        consumer.setInputQueue("input-queue");
        consumer.setS3Bucket("bucket");
        consumer.setS3Key("dev/img");
        consumer.setHeight(220);
        consumer.setWidth(440);
    }

    @Test
    public void shouldGenerateThumbnail() throws Exception {
        // Given
        final Image img0 = createImage(new Link());
        when(imageRepository.findOne(123L)).thenReturn(img0);

        final S3ObjectInputStream in = createS3InputStream("image-content");
        final S3Object obj = mock(S3Object.class);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject(anyString(), anyString())).thenReturn(obj);

        doAnswer(resize("/image/jordan.jpg")).when(imageProcessorService)
                .resize(anyInt(), anyInt(), any(InputStream.class), any(OutputStream.class), anyString());

        // When
        consumer.consumeMessage("123");

        // Then
        verify(s3).putObject(
                eq("bucket"),
                eq("dev/img/2011/10/11/test-" + Image.TYPE_MAIN + ".png"),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );

        final ArgumentCaptor<Image> img = ArgumentCaptor.forClass(Image.class);
        verify(imageRepository).save(img.capture());
        assertThat(img.getValue().getLink()).isEqualTo(img0.getLink());
        assertThat(img.getValue().getS3Key()).isEqualTo("dev/img/2011/10/11/test-" + Image.TYPE_MAIN + ".png");
        assertThat(img.getValue().getType()).isEqualTo(Image.TYPE_MAIN);
        assertThat(img.getValue().getUrl()).isEqualTo(img0.getUrl());
        assertThat(img.getValue().getContentType()).isEqualTo("image/png");
        assertThat(img.getValue().getWidth()).isEqualTo(440);
        assertThat(img.getValue().getHeight()).isEqualTo(220);
    }

    @Test
    public void shouldMaintainAspectRatio() throws Exception {
        // Given
        final Image img0 = createImage(new Link());
        img0.setWidth(1000);
        img0.setHeight(333);
        when(imageRepository.findOne(123L)).thenReturn(img0);

        final S3ObjectInputStream in = createS3InputStream("image-content");
        final S3Object obj = mock(S3Object.class);
        when(obj.getObjectContent()).thenReturn(in);
        when(s3.getObject(anyString(), anyString())).thenReturn(obj);

        doAnswer(resize("/image/jordan.jpg")).when(imageProcessorService)
                .resize(anyInt(), anyInt(), any(InputStream.class), any(OutputStream.class), anyString());

        // When
        consumer.consumeMessage("123");

        // Then
        final ArgumentCaptor<Image> img = ArgumentCaptor.forClass(Image.class);
        verify(imageRepository).save(img.capture());
        assertThat(img.getValue().getWidth()).isEqualTo(440);
        assertThat(img.getValue().getHeight()).isEqualTo(146);
    }

    private Image createImage(final Link link) {
        final Image img = new Image();
        img.setContentLength(1024L);
        img.setContentType("image/png");
        img.setHeight(500);
        img.setLink(link);
        img.setS3Key("dev/img/2011/10/11/test.png");
        img.setType(Image.TYPE_ORIGINAL);
        img.setUrl("http://www.goo.com/test.png");
        img.setWidth(1000);

        return img;
    }

    public Answer resize(final String path) {
        return new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final OutputStream out = (OutputStream) invocationOnMock.getArguments()[3];
                final InputStream in = getClass().getResourceAsStream(path);
                IOUtils.copy(in, out);
                return null;
            }
        };
    }
}
