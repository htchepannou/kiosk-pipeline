package io.tchepannou.kiosk.pipeline.service;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.tchepannou.kiosk.pipeline.Fixtures.createFeed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class UrlServiceTest {
    @Mock
    HttpService http;

    @InjectMocks
    UrlService service;

    @Test
    public void shouldExtractUrls() throws Exception {
        // Given
        final Feed feed = createFeed("test", "http://www.google.ca", null);

        final String html = "<body>"
                + "<a href='http://www.google.ca/article/Test_123.html'>"
                + "<a href='http://www.google.ca/category/SPORT/'>"
                + "<a href='http://www.google.ca/category/TECH/'>"
                + "</body>";

        doAnswer(get(html)).when(http).get(any(), any());

        // When
        Collection<String> result = service.extractUrls(feed);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).contains(
                "http://www.google.ca/article/test_123.html",
                "http://www.google.ca/category/sport",
                "http://www.google.ca/category/tech"
        );

    }

    @Test
    public void shouldExtractUrlsThatMatchPattern() throws Exception {
        // Given
        final Feed feed = createFeed("test", "http://www.google.ca", "*/category/*");

        final String html = "<body>"
                + "<a href='http://www.google.ca/article/Test_123.html'>"
                + "<a href='http://www.google.ca/category/SPORT/'>"
                + "<a href='http://www.google.ca/category/TECH/'>"
                + "</body>";

        doAnswer(get(html)).when(http).get(any(), any());

        // When
        Collection<String> result = service.extractUrls(feed);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(
                "http://www.google.ca/category/sport",
                "http://www.google.ca/category/tech"
        );

    }
    
    @Test
    public void shouldExtractRelativeUrls() throws Exception {
        // Given
        final Feed feed = createFeed("test", "http://www.google.ca", null);

        final String html = "<body>"
                + "<a href='/article/Test_123.html'>"
                + "<a href='/category/SPORT/'>"
                + "<a href='/category/TECH/'>"
                + "</body>";

        doAnswer(get(html)).when(http).get(any(), any());

        // When
        Collection<String> result = service.extractUrls(feed);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).contains(
                "http://www.google.ca/category/sport",
                "http://www.google.ca/category/tech"
        );

    }
    
    @Test
    public void shouldExcludeUrlFromOtherFeed() throws Exception {
        // Given
        final Feed feed = createFeed("test", "http://www.google.ca", null);

        final String html = "<body>"
                + "<a href='http://www.google.ca/article/Test_123.html'>"
                + "<a href='http://www.google.ca/category/SPORT/'>"
                + "<a href='http://www.google.ca/category/TECH/'>"
                + "<a href='http://www.facebook.com/category/TECH/'>"
                + "</body>";

        doAnswer(get(html)).when(http).get(any(), any());

        // When
        Collection<String> result = service.extractUrls(feed);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).contains(
                "http://www.google.ca/category/sport",
                "http://www.google.ca/category/tech"
        );

    }

    @Test
    public void testNormalize() throws Exception {
        assertThat(service.normalize(null)).isEqualTo("");
        assertThat(service.normalize("http://www.google.ca")).isEqualTo("http://www.google.ca");
        assertThat(service.normalize("http://WWW.google.CA")).isEqualTo("http://www.google.ca");
        assertThat(service.normalize("http://www.google.ca/")).isEqualTo("http://www.google.ca");
        assertThat(service.normalize(" http://www.google.ca/ ")).isEqualTo("http://www.google.ca");
    }

    @Test
    public void testIsBlacklisted() throws Exception {
        // Given
        final List<String> urls = new ArrayList<>();
        urls.add("*/wp-login.php*");
        urls.add("*/feed/rss");
        urls.add("*#*");
        urls.add("*/\\d{4}/\\d{2}/\\d{2}");
        service.setBlacklist(urls);

        // Then
        assertThat(service.isBlacklisted(null)).isTrue();
        assertThat(service.isBlacklisted("")).isTrue();
        assertThat(service.isBlacklisted("http://www.sparkcameroun.com/wp-login.php?action=register")).isTrue();
        assertThat(service.isBlacklisted("http://www.sparkcameroun.com/wp-login.php")).isTrue();
        assertThat(service.isBlacklisted("http://www.camer24.de/feed/rss")).isTrue();

        assertThat(service.isBlacklisted(
                "http://www.jewanda-magazine.com/2016/01/video-eudoxie-yao-mon-physique-de-reve-ma-ouvert-beaucoup-de-portes/#comment-216278")).isTrue();
        assertThat(service.isBlacklisted("http://www.jewanda-magazine.com/2017/01/29")).isTrue();
        assertThat(service.isBlacklisted(
                "http://www.jewanda-magazine.com/2016/01/video-eudoxie-yao-mon-physique-de-reve-ma-ouvert-beaucoup-de-portes/")).isFalse();

        assertThat(service.isBlacklisted("http://www.sparkcameroun.com")).isFalse();

    }    
    private Answer get(final String html) {
        return (inv) -> {
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            out.write((html).getBytes());
            return null;
        };
    }
}
