package io.tchepannou.kiosk.pipeline.aws.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsSnsConsumerTest {
    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    ConsumerImpl consumer;


    @Test
    public void testConsume() throws Exception {
        // given
        final String body = "This is a sample body";

        final Map notification = new HashMap<>();
        notification.put("Message", "foo");
        when(objectMapper.readValue(body, Object.class)).thenReturn(notification);

        // When
        ConsumerImpl xconsumer = spy(consumer);
        xconsumer.consume(body);

        // Then
        verify(xconsumer).consumeMessage("foo");
    }

    @Test
    public void shouldNotConsumeInvalidMessage() throws Exception{
        // given
        final String body = "This is a sample body";

        final Map notification = new HashMap<>();
        when(objectMapper.readValue(body, Object.class)).thenReturn(notification);

        // When
        ConsumerImpl xconsumer = spy(consumer);
        xconsumer.consume(body);

        // Then
        verify(xconsumer, never()).consumeMessage("foo");
    }

    public static class ConsumerImpl extends SqsSnsConsumer{
        @Override
        protected void consumeMessage(final String message) throws IOException {
        }
    }
}
