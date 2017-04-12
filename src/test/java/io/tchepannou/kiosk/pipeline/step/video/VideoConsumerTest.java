package io.tchepannou.kiosk.pipeline.step.video;

import io.tchepannou.kiosk.pipeline.persistence.domain.Asset;
import io.tchepannou.kiosk.pipeline.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.step.metadata.TagService;
import io.tchepannou.kiosk.pipeline.step.LinkConsumerTestSupport;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VideoConsumerTest extends LinkConsumerTestSupport {
    @Mock
    private List<VideoProvider> providers;

    @Mock
    private TagService tagService;

    @Mock
    private VideoProvider youtube;

    @Mock
    private VideoProvider vimeo;

    @InjectMocks
    VideoConsumer consumer;

    @Before
    public void setUp() {
        consumer.setProviders(Arrays.asList(vimeo, youtube));
    }

    @Test
    public void shouldExtractVideo() throws Exception {
        // Given
        final Link link = new Link();

        doAnswer(read("/video/mbokotv.html")).when(repository).read(anyString(), any());

        doAnswer(save(567)).when(linkRepository).save(any(Link.class));

        when(youtube.getEmbedUrl(anyString())).thenReturn("http://you.be/4309430");

        final Date publishedDate = DateUtils.addDays(new Date(), -10);
        final VideoInfo info = new VideoInfo();
        info.setTitle("This is the title");
        info.setDescription("This is the long description of video");
        info.setPublishedDate(publishedDate);
        info.setTags(Arrays.asList("tag1", "tag2", "tag3"));
        when(youtube.getInfo(anyString())).thenReturn(info);

        // Then
        consumer.consume(link);

        // Then
        final ArgumentCaptor<Link> video = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(video.capture());

        assertThat(video.getValue().getS3Key()).isNullOrEmpty();
        assertThat(video.getValue().getType()).isEqualTo(LinkTypeEnum.video);
        assertThat(video.getValue().getUrl()).isEqualTo("http://you.be/4309430");
        assertThat(video.getValue().getUrlHash()).isEqualTo("b2a9cf13c361c53e3a203e1c458cf6c9");
        assertThat(video.getValue().getStatus()).isEqualTo(LinkStatusEnum.valid);
        assertThat(video.getValue().getTitle()).isEqualTo("This is the title");
        assertThat(video.getValue().getSummary()).isEqualTo("This is the long description of video");
        assertThat(video.getValue().getPublishedDate()).isEqualTo(publishedDate);

        final ArgumentCaptor<Asset> asset = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(asset.capture());
        assertThat(asset.getValue().getLink()).isEqualTo(link);
        assertThat(asset.getValue().getTarget()).isEqualTo(video.getValue());
        assertThat(asset.getValue().getType()).isEqualTo(AssetTypeEnum.video);

        verify(tagService).tag(video.getValue(), Arrays.asList("tag1", "tag2", "tag3"));
    }

    @Test
    public void shouldUpdateVideo() throws Exception {
        // Given
        final Link link = new Link();

        when(linkRepository.findByUrlHash(anyString())).thenReturn(link);

        doAnswer(read("/video/mbokotv.html")).when(repository).read(anyString(), any());

        doAnswer(save(567)).when(linkRepository).save(any(Link.class));

        when(youtube.getEmbedUrl(anyString())).thenReturn("http://you.be/4309430");

        final Date publishedDate = DateUtils.addDays(new Date(), -10);
        final VideoInfo info = new VideoInfo();
        info.setTitle("This is the title");
        info.setDescription("This is the long description of video");
        info.setPublishedDate(publishedDate);
        info.setTags(Arrays.asList("tag1", "tag2", "tag3"));
        when(youtube.getInfo(anyString())).thenReturn(info);

        // Then
        consumer.consume(link);

        // Then
        final ArgumentCaptor<Link> video = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(video.capture());

        assertThat(video.getValue().getTitle()).isEqualTo("This is the title");
        assertThat(video.getValue().getSummary()).isEqualTo("This is the long description of video");
        assertThat(video.getValue().getPublishedDate()).isEqualTo(publishedDate);

        final ArgumentCaptor<Asset> asset = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(asset.capture());
        assertThat(asset.getValue().getLink()).isEqualTo(link);
        assertThat(asset.getValue().getTarget()).isEqualTo(video.getValue());
        assertThat(asset.getValue().getType()).isEqualTo(AssetTypeEnum.video);

        verify(tagService).tag(video.getValue(), Arrays.asList("tag1", "tag2", "tag3"));
    }
}
