package io.tchepannou.kiosk.pipeline.step.image;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Asset;
import io.tchepannou.kiosk.pipeline.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.repository.AssetRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.step.LinkConsumerTestSupport;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImageConsumerTest extends LinkConsumerTestSupport  {
    @Mock
    MessageQueue queue;

    @Mock
    AssetRepository assetRepository;

    @Mock
    HttpService http;

    @InjectMocks
    ImageConsumer consumer;

    @Before
    public void setUp (){
        consumer.setImageFolder("image");
        consumer.setRawFolder("html");
    }

    @Test
    public void shouldExtractImage() throws Exception {
        // Given
        final Link link = createLink(123, "html/2010/10/11/test.html");

        doAnswer(read("/image/article.html")).when(repository).read(any(), any());

        doAnswer(get("/image/jordan.jpg", "image/jpeg")).when(http).get(any(), any());

        doAnswer(save(567)).when(linkRepository).save(any(Link.class));

        // Then
        consumer.consume(link);

        // Then
        verify(repository).write(
                eq("image/2010/10/11/test.jpg"),
                any(InputStream.class)
        );

        final ArgumentCaptor<Link> img = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(img.capture());
        assertThat(img.getValue().getS3Key()).isEqualTo("image/2010/10/11/test.jpg");
        assertThat(img.getValue().getType()).isEqualTo(LinkTypeEnum.image.name());
        assertThat(img.getValue().getUrl()).isEqualTo("http://camfoot.com/IMG/arton25520.jpg");
        assertThat(img.getValue().getContentType()).isEqualTo("image/jpeg");
        assertThat(img.getValue().getContentLength()).isEqualTo(4490750);
        assertThat(img.getValue().getWidth()).isEqualTo(2400);
        assertThat(img.getValue().getHeight()).isEqualTo(3000);
        assertThat(img.getValue().getUrlHash()).isEqualTo("0c63fbc8bc07385c263a8637793c3a9e");

        final ArgumentCaptor<Asset> asset = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(asset.capture());
        assertThat(asset.getValue().getLink()).isEqualTo(link);
        assertThat(asset.getValue().getTarget()).isEqualTo(img.getValue());
        assertThat(asset.getValue().getType()).isEqualTo(AssetTypeEnum.original.name());

        verify(queue).push("567");
    }

    @Test
    public void shouldNeverOverwriteImage() throws Exception {
        // Given
        final Link link = createLink(123, "html/2010/10/11/test.html");

        doAnswer(read("/image/article.html")).when(repository).read(any(), any());

        doAnswer(get("/image/jordan.jpg", "image/jpeg")).when(http).get(any(), any());

        final Link img = new Link();
        img.setId(567);
        when(linkRepository.findByUrlHash(anyString())).thenReturn(img);

        // Then
        consumer.consume(link);

        // Then
        verify(repository, never()).write(any(), any());

        verify(linkRepository, never()).save(img);

        verify(assetRepository).save(any(Asset.class));

        verify(queue).push("567");
    }

    @Test
    public void shouldNotConsumetNonImage() throws Exception {
        // Given
        final Link link = createLink(123, "html/2010/10/11/test.html");

        doAnswer(read("/image/article.html")).when(repository).read(any(), any());

        doAnswer(get("/image/article.html", "text/html")).when(http).get(any(), any());

        // Then
        consumer.consume(link);

        // Then
        verify(repository, never()).write(any(), any());

        verify(linkRepository, never()).save(any(Link.class));

        verify(assetRepository, never()).save(any(Asset.class));

        verify(queue, never()).push(anyString());
    }

    private Link createLink (final long id, final String s3Key){
        final Link link = new Link();
        link.setId(id);
        link.setS3Key(s3Key);
        return link;
    }

    //-- Private
    private Answer save(final long id) {
        return (inv) -> {
            final Link img = (Link) inv.getArguments()[0];
            if (img.getId() == 0) {
                img.setId(id);
            }
            return null;
        };
    }

    private Answer get(final String path, final String contentType) {
        return (inv) -> {
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            final InputStream in = getClass().getResourceAsStream(path);
            IOUtils.copy(in, out);
            return contentType;
        };
    }

    private Answer read(final String path) {
        return (inv) -> {
            final InputStream in = getClass().getResourceAsStream(path);
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            IOUtils.copy(in, out);
            return null;
        };
    }

}
