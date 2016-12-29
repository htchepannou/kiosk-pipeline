package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.UrlBlacklistService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.OutputStream;

import static io.tchepannou.kiosk.pipeline.Fixtures.createFeed;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UrlExtractorConsumerTest {
    private static final String HTML = "<body>"
            + "<a href='/article/test_123.html'>"
            + "<a href='http://www.google.ca/article/test_456.html'>"
            + "<a href='http://www.google.ca/link.html'>"
            + "</body>";

    @Mock
    AmazonSQS sqs;

    @Mock
    HttpService http;

    @Mock
    FeedRepository feedRepository;

    @Mock
    LinkRepository linkRepository;

    @Mock
    UrlBlacklistService urlBlacklistService;

    @InjectMocks
    UrlExtractorConsumer consumer;


    @Before
    public void setUp (){
        consumer.setOutputQueue("output-queue");
    }

    @Test
    public void shouldExtractUrls() throws Exception {
        // Given
        final Feed feed = createFeed("test", "http://www.google.ca", "/article/*.html");
        when(feedRepository.findOne(feed.getId())).thenReturn(feed);

        doAnswer(get(HTML)).when(http).get(any(), any());

        // When
        consumer.consume(String.valueOf(feed.getId()));

        // Then
        verify(sqs).sendMessage("output-queue", "http://www.google.ca/article/test_123.html");
        verify(sqs).sendMessage("output-queue", "http://www.google.ca/article/test_456.html");
    }

    @Test
    public void shouldNoExtractUrlAlreadyDownloaded() throws Exception {
        // Given
        final Feed feed = createFeed("test", "http://www.google.ca", "/article/*.html");
        when(feedRepository.findOne(feed.getId())).thenReturn(feed);

        doAnswer(get(HTML)).when(http).get(any(), any());

        final String hash = Link.hash("http://www.google.ca/article/test_123.html");
        when(linkRepository.findByUrlHash(hash)).thenReturn(new Link());

        // When
        consumer.consume(String.valueOf(feed.getId()));

        // Then
        verify(sqs, never()).sendMessage("output-queue", "http://www.google.ca/article/test_123.html");
        verify(sqs).sendMessage("output-queue", "http://www.google.ca/article/test_456.html");
    }

    @Test
    public void shouldNoExtractUrlBlacklisted() throws Exception {
        // Given
        final Feed feed = createFeed("test", "http://www.google.ca", "/article/*.html");
        when(feedRepository.findOne(feed.getId())).thenReturn(feed);

        doAnswer(get(HTML)).when(http).get(any(), any());

        when(urlBlacklistService.contains("http://www.google.ca/article/test_123.html")).thenReturn(true);

        // When
        consumer.consume(String.valueOf(feed.getId()));

        // Then
        verify(sqs, never()).sendMessage("output-queue", "http://www.google.ca/article/test_123.html");
        verify(sqs).sendMessage("output-queue", "http://www.google.ca/article/test_456.html");
    }

    private Answer get(final String html) {
        return (inv) -> {
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            out.write((html).getBytes());
            return null;
        };
    }
}
