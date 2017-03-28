package io.tchepannou.kiosk.pipeline.service.video;

import io.tchepannou.kiosk.pipeline.step.video.VideoProvider;
import io.tchepannou.kiosk.pipeline.step.video.providers.YouTube;
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
    VideoProvider videoService;

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

        final VideoProvider s1 = mock(VideoProvider.class);

        final String url = "http://www.youtube.com/embed/c6YN-X-YvEQ";
        final VideoProvider s2 = mock(VideoProvider.class);
//        when(s2.getVideoId("http://www.youtube.com/embed/c6YN-X-YvEQ?enablejsapi=1&feature=oembed&wmode=opaque&vq=hd720")).thenReturn("c6YN-X-YvEQ");
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
