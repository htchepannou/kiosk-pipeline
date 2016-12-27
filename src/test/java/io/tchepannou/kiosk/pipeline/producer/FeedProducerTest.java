package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeedProducerTest {
    @Mock
    AmazonSQS sqs;

    @Mock
    FeedRepository feedRepository;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    FeedProducer processor;

    @Before
    public void setUp() {
        processor.setOutputQueue("feed-queue");
    }

    @Test
    public void shouldProcess() throws Exception {
        // Given
        final Feed f1 = new Feed();
        f1.setId(1);

        final Feed f2 = new Feed();
        f2.setId(2);
        when(feedRepository.findAll()).thenReturn(Arrays.asList(f1, f2));

        // When
        processor.produce();

        // Then
        verify(sqs).sendMessage("feed-queue", "1");
        verify(sqs).sendMessage("feed-queue", "2");
    }

}
