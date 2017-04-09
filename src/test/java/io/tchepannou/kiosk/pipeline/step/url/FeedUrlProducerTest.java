package io.tchepannou.kiosk.pipeline.step.url;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeedUrlProducerTest {
    private static final List<String> URLS = Arrays.asList("http://www.foo.com/1", "http://www.foo.com/2");
    @Mock
    MessageQueue output;

    @Mock
    UrlService urlService;

    @Mock
    LinkRepository linkRepository;

    @Mock
    Feed feed;

    @InjectMocks
    private FeedUrlProducer producer;

    @Before
    public void setUp () throws Exception {
        when(urlService.extractUrls(any())).thenReturn(URLS);
        when(feed.isActive()).thenReturn(true);
    }

    @Test
    public void shouldPushURLToOutput() throws Exception {
        // When
        producer.produce(feed);

        // Then
        verify(output).push(URLS.get(0));
        verify(output).push(URLS.get(1));
    }

    @Test
    public void shouldNeverProduceUrlForInactiveFeeds() throws Exception {
        // Given
        when(feed.isActive()).thenReturn(false);

        // When
        producer.produce(feed);

        // Then
        verify(output, never()).push(anyString());
    }

    @Test
    public void shouldNeverPushFeedHomepage() throws Exception {
        // Given
        when(feed.getUrl()).thenReturn(URLS.get(0));

        // When
        producer.produce(feed);

        // Then
        verify(output, never()).push(URLS.get(0));
        verify(output).push(URLS.get(1));
    }

    @Test
    public void shouldNeverPushDownloadedUrl() throws Exception {
        // Given
        when(linkRepository.findByUrlHash(Link.hash(URLS.get(0)))).thenReturn(new Link());

        // When
        producer.produce(feed);

        // Then
        verify(output, never()).push(URLS.get(0));
        verify(output).push(URLS.get(1));
    }

    @Test
    public void shouldNeverPushBlacklistedUrl() throws Exception {
        // Given
        when(urlService.isBlacklisted(URLS.get(0))).thenReturn(true);

        // When
        producer.produce(feed);

        // Then
        verify(output, never()).push(URLS.get(0));
        verify(output).push(URLS.get(1));
    }
}
