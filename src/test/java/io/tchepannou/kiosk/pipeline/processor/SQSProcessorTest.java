package io.tchepannou.kiosk.pipeline.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SQSProcessorTest {
    @Mock
    AmazonSQS sqs;

    SQSProcessor processor;

    List<String> bodies;

    @Before
    public void setUp() {
        bodies = new ArrayList<>();

        processor = new Processor();
        processor.sqs = sqs;
    }

    @Test
    public void shouldProcessMessages() throws Exception {
        // Given
        final Message msg1 = createMessage("1", "m1");
        final Message msg2 = createMessage("2", "m2");
        final Message msg3 = createMessage("3", "m3");
        final ReceiveMessageResult result = createReceiveMessageResult(msg1, msg2, msg3);
        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(result)
                .thenReturn(createReceiveMessageResult())
        ;

        // When
        processor.process();

        // Then
        assertThat(bodies).containsExactly("m1", "m2", "m3");
        verify(sqs).deleteMessage("input-queue", "1");
        verify(sqs).deleteMessage("input-queue", "2");
        verify(sqs).deleteMessage("input-queue", "3");
    }

    @Test
    public void shouldIgnoreException() throws Exception {
        // Given
        final Message msg1 = createMessage("1", "exception");
        final Message msg2 = createMessage("2", "m2");
        final Message msg3 = createMessage("3", "m3");
        final ReceiveMessageResult result = createReceiveMessageResult(msg1, msg2, msg3);
        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(result)
                .thenReturn(createReceiveMessageResult())
        ;

        // When
        processor.process();

        // Then
        assertThat(bodies).containsExactly("m2", "m3");
        verify(sqs).deleteMessage("input-queue", "2");
        verify(sqs).deleteMessage("input-queue", "3");
    }

    private ReceiveMessageResult createReceiveMessageResult(final Message... messages) {
        final ReceiveMessageResult result = mock(ReceiveMessageResult.class);
        when(result.getMessages()).thenReturn(Arrays.asList(messages));
        return result;
    }

    private Message createMessage(final String handle, final String body) {
        final Message msg = mock(Message.class);
        when(msg.getReceiptHandle()).thenReturn(handle);
        when(msg.getBody()).thenReturn(body);
        return msg;
    }

    public class Processor extends SQSProcessor {
        @Override
        protected void process(final String body) throws IOException {
            if ("exception".equals(body)){
                throw new IllegalStateException(body);
            }
            bodies.add(body);
        }

        @Override
        public String getInputQueue() {
            return "input-queue";
        }
    }
}
