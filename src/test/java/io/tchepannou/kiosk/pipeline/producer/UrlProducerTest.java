package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.Executor;

import static io.tchepannou.kiosk.pipeline.Fixtures.createFeed;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UrlProducerTest {
    @Mock
    FeedRepository feedRepository;

    @Mock
    LinkRepository linkRepository;

    @Mock
    Executor executor;

    @Mock
    AmazonSQS sqs;

    @InjectMocks
    UrlProducer producer;

    @Test
    public void testProduce() throws Exception {
        final Feed f1 = createFeed();
        final Feed f2 = createFeed();
        final Feed f3 = createFeed();
        when(feedRepository.findAll()).thenReturn(Arrays.asList(f1, f2, f3));

        producer.produce();

        verify(executor, times(3)).execute(any(Runnable.class));
    }


}
