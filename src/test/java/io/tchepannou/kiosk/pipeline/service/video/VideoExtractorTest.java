package io.tchepannou.kiosk.pipeline.service.video;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VideoExtractorTest {
    @Mock
    VideoService videoService;

    @Test
    public void shouldExtractLinks ( ) throws Exception {
        // Given
        final String url = "http://www.youtube.com/embed/c6YN-X-YvEQ?enablejsapi=1&feature=oembed&wmode=opaque&vq=hd720";
        final String embedUrl = "http://www.youtube.com/embed/c6YN-X-YvEQ";

        final String html = "<html><body>" +
                "<iframe " +
                "id=\"td_youtube_player\" " +
                "width=\"600\" " +
                "height=\"560\" " +
                "src=\"http://www.youtube.com/embed/c6YN-X-YvEQ?enablejsapi=1&feature=oembed&wmode=opaque&vq=hd720\" " +
                "frameborder=\"0\" " +
                "allowfullscreen=\"\">" +
                "</iframe>\n"+
                "</body></html>";

        when(videoService.getVideoId(url)).thenReturn("c6YN-X-YvEQ");
        when(videoService.getEmbedUrl("c6YN-X-YvEQ")).thenReturn(embedUrl);
        when(videoService.getName()).thenReturn("youtube");

        final Feed feed = new Feed();

        // When
        final List<Link> result = new VideoExtractor(Arrays.asList(videoService)).extractLinks(html, feed);

        // Then
        assertThat(result).hasSize(1);

        final Link link = result.get(0);
        assertThat(link.getDisplayTitle()).isNull();
        assertThat(link.getEmbedUrl()).isEqualTo(embedUrl);
        assertThat(link.getFeed()).isEqualTo(feed);
        assertThat(link.getId()).isEqualTo(0);
        assertThat(link.getProvider()).isEqualTo("youtube");
        assertThat(link.getS3Key()).isNull();
        assertThat(link.getStatus()).isEqualTo(Link.STATUS_CREATED);
        assertThat(link.getSummary()).isNull();
        assertThat(link.getTitle()).isNullOrEmpty();
        assertThat(link.getType()).isEqualTo(LinkTypeEnum.video);
        assertThat(link.getUrl()).isEqualTo(url);
        assertThat(link.getUrlHash()).isEqualTo("6366a2e0cf7be351efe02c2ff3512615");
    }

    @Test
    public void shouldExtractVideoEmbedUrl() throws Exception {
        // Given
        final String html = "<html><body>" +
                "<iframe " +
                    "id=\"td_youtube_player\" " +
                    "width=\"600\" " +
                    "height=\"560\" " +
                    "src=\"http://www.youtube.com/embed/c6YN-X-YvEQ?enablejsapi=1&feature=oembed&wmode=opaque&vq=hd720\" " +
                    "frameborder=\"0\" " +
                    "allowfullscreen=\"\">" +
                    "</iframe>\n"+
                "</body></html>";

        final VideoService s1 = mock(VideoService.class);

        final String url = "http://www.youtube.com/embed/c6YN-X-YvEQ";
        final VideoService s2 = mock(VideoService.class);
        when(s2.getVideoId("http://www.youtube.com/embed/c6YN-X-YvEQ?enablejsapi=1&feature=oembed&wmode=opaque&vq=hd720")).thenReturn("c6YN-X-YvEQ");
        when(s2.getEmbedUrl("c6YN-X-YvEQ")).thenReturn(url);

        // When
        final List<String> result = new VideoExtractor(Arrays.asList(s1, s2)).extract(html);

        // Then
        assertThat(result).contains(url);
    }

    @Test
    public void shouldExtractVideoFromMbokoTV() throws Exception {
        // Given
        final String html = IOUtils.toString(getClass().getResourceAsStream("/video/mbokotv.html"));
        final VideoExtractor extractor = new VideoExtractor(Arrays.asList(new YouTube()));

        // When
        final List<String> result = extractor.extract(html);

        // Then
        assertThat(result).contains("https://www.youtube.com/embed/c6YN-X-YvEQ");
    }


    @Test
    public void shouldExtractVideoFromJeWandaTV() throws Exception {
        // Given
        final String html = IOUtils.toString(getClass().getResourceAsStream("/video/jewanda.html"));
        final VideoExtractor extractor = new VideoExtractor(Arrays.asList(new YouTube()));

        // When
        final List<String> result = extractor.extract(html);

        // Then
        assertThat(result).containsExactly(
                "https://www.youtube.com/embed/9QQbAMYgQ7s",
                "https://www.youtube.com/embed/MYahouq-eC4",
                "https://www.youtube.com/embed/nFrNqDtoB3E",
                "https://www.youtube.com/embed/8-19-ayUboU",
                "https://www.youtube.com/embed/UVxeQSoMJ_Y"
        );
    }
}
