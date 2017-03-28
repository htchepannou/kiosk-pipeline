package io.tchepannou.kiosk.pipeline.step.download;

import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.InvalidContentTypeException;
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
public class DownloadConsumerTest {
    @Mock
    HttpService http;

    @Mock
    FileRepository repository;

    @Mock
    MessageQueue queue;

    @Mock
    Clock clock;

    @Mock
    LinkRepository linkRepository;

    @Mock
    FeedRepository feedRepository;

    @InjectMocks
    DownloadConsumer consumer;

    Feed feed1;
    Feed feed2;

    @Before
    public void setUp() throws Exception {
        consumer.setFolder("html");

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
        verify(repository).write(
                eq("html/2013/04/05/13/" + key + ".html"),
                in.capture()
        );
        assertThat(IOUtils.toString(in.getValue())).isEqualTo("hello");

        final ArgumentCaptor<Link> link = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(link.capture());
        assertThat(link.getValue().getUrl()).isEqualTo(url);
        assertThat(link.getValue().getUrlHash()).isEqualTo(Link.hash(url));
        assertThat(link.getValue().getS3Key()).isEqualTo("html/2013/04/05/13/" + key + ".html");
        assertThat(link.getValue().getFeed()).isEqualTo(feed1);
        assertThat(link.getValue().getStatus()).isEqualTo(LinkStatusEnum.created);

        verify(queue).push(String.valueOf(link.getValue().getId()));
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
        verify(repository).write(any(), any());
        verify(queue, never()).push(anyString());
    }

    @Test
    public void shouldNotConsumeNonHtml() throws Exception {
        // Given
        final String url = "http://www.goo.com/test.html";
        doThrow(InvalidContentTypeException.class).when(http).getHtml(eq(url), any(OutputStream.class));

        when(linkRepository.save(any(Link.class))).thenThrow(new DataIntegrityViolationException("error"));

        // Given
        consumer.consume(url);

        // Then
        verify(repository, never()).write(any(), any());
        verify(queue, never()).push(anyString());
        verify(linkRepository, never()).save(any(Link.class));
    }

    private Answer get(final String str) {
        return (inv) -> {
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            out.write(str.getBytes());
            return null;
        };
    }

}
