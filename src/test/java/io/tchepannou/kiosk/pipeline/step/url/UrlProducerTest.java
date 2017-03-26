package io.tchepannou.kiosk.pipeline.step.url;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.step.UrlProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UrlProducerTest {
    @Mock
    FeedRepository feedRepository;

    @Mock
    FeedUrlProducer feedUrlProducer;

    @InjectMocks
    UrlProducer producer;

    @Test
    public void testProduce() throws Exception {
        // Given
        Feed feed1 = new Feed();
        Feed feed2 = new Feed();
        Feed feed3 = new Feed();
        when(feedRepository.findAll()).thenReturn(Arrays.asList(feed1, feed2, feed3));

        // When
        producer.produce();

        // Then
        verify(feedUrlProducer).produce(feed1);
        verify(feedUrlProducer).produce(feed2);
        verify(feedUrlProducer).produce(feed3);
    }
}
