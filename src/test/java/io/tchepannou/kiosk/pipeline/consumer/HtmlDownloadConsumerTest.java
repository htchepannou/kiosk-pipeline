package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.sns.AmazonSNS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.InvalidContentTypeException;
import io.tchepannou.kiosk.pipeline.service.UrlBlacklistService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Arrays;
import java.util.Date;

import static io.tchepannou.kiosk.pipeline.Fixtures.createFeed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlDownloadConsumerTest {
    @Mock
    AmazonS3 s3;

    @Mock
    AmazonSNS sns;

    @Mock
    HttpService http;

    @Mock
    Clock clock;

    @Mock
    LinkRepository linkRepository;

    @Mock
    UrlBlacklistService urlBlacklistService;

    @Mock
    FeedRepository feedRepository;

    @InjectMocks
    HtmlDownloadConsumer consumer;

    Feed feed1;
    Feed feed2;

    @Before
    public void setUp() throws Exception {
        consumer.setS3Bucket("bucket");
        consumer.setS3Key("html");
        consumer.setOutputTopic("topic");

        final Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-04-05 13:20:50");
        when(clock.millis()).thenReturn(date.getTime());

        feed1 = createFeed("feed1", "http://www.goo.com", null);
        feed2 = createFeed("feed2", "http://www.yahoo.com", null);
        when(feedRepository.findAll()).thenReturn(Arrays.asList(feed1, feed2));
        consumer.init();
    }

    @Test
    public void shouldConsumeMessage() throws Exception {
        // Given
        final String url = "http://www.goo.com/test.html";
        final String key = DigestUtils.md5Hex(url);

        doAnswer(get("hello")).when(http).getHtml(eq(url), any(OutputStream.class));

        when(linkRepository.findByUrlHash(any())).thenReturn(null);

        // Given
        consumer.consume(url);

        // Then
        final ArgumentCaptor<InputStream> in = ArgumentCaptor.forClass(InputStream.class);
        verify(s3).putObject(
                eq("bucket"),
                eq("html/2013/04/05/13/" + key + ".html"),
                in.capture(),
                any(ObjectMetadata.class)
        );
        assertThat(IOUtils.toString(in.getValue())).isEqualTo("hello");

        final ArgumentCaptor<Link> link = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(link.capture());
        assertThat(link.getValue().getUrl()).isEqualTo(url);
        assertThat(link.getValue().getUrlHash()).isEqualTo(Link.hash(url));
        assertThat(link.getValue().getS3Key()).isEqualTo("html/2013/04/05/13/" + key + ".html");
        assertThat(link.getValue().getFeed()).isEqualTo(feed1);

        verify(sns).publish("topic", String.valueOf(link.getValue().getId()));
    }

    @Test
    public void shouldNotConsumeMessageAlreadyDownloaded() throws Exception {
        // Given
        final String url = "http://www.goo.com/test.html";
        doAnswer(get("hello")).when(http).getHtml(eq(url), any(OutputStream.class));

        when(linkRepository.findByUrlHash(any())).thenReturn(new Link());

        // Given
        consumer.consume(url);

        // Then
        verify(s3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        verify(linkRepository, never()).save(any(Link.class));
        verify(sns, never()).publish(anyString(), anyString());
    }

    @Test
    public void shouldNotConsumeMessageIfDataIntegrityViolationException() throws Exception {
        // Given
        final String url = "http://www.goo.com/test.html";
        doAnswer(get("hello")).when(http).getHtml(eq(url), any(OutputStream.class));

        when(linkRepository.save(any(Link.class))).thenThrow(new DataIntegrityViolationException("error"));

        // Given
        consumer.consume(url);

        // Then
        verify(s3).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        verify(linkRepository).save(any(Link.class));
        verify(sns, never()).publish(anyString(), anyString());
    }

    @Test
    public void shouldNotConsumeNonHtmlUrl() throws Exception {
        // Given
        final String url = "http://www.goo.com/test.html";
        final InvalidContentTypeException ex = new InvalidContentTypeException("error");
        doThrow(ex).when(http).getHtml(eq(url), any(OutputStream.class));

        // Given
        consumer.consume(url);

        // Then
        verify(s3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        verify(linkRepository, never()).save(any(Link.class));
        verify(sns, never()).publish(anyString(), anyString());
    }

    @Test
    public void shouldNotConsumeBlacklistedUrl() throws Exception {
        // Given
        final String url = "http://www.goo.com/test.html";
        when(urlBlacklistService.contains(url)).thenReturn(true);

        // Given
        consumer.consume(url);

        // Then
        verify(s3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        verify(linkRepository, never()).save(any(Link.class));
        verify(sns, never()).publish(anyString(), anyString());
    }

    private Answer get(final String str) {
        return (inv) -> {
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            out.write(str.getBytes());
            return null;
        };
    }
}
