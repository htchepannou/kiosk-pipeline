package io.tchepannou.kiosk.pipeline.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.tchepannou.kiosk.pipeline.Fixtures.createSqsMessage;
import static io.tchepannou.kiosk.pipeline.Fixtures.createSqsReceiveMessageResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsReaderTest {
    @Mock
    AmazonSQS sqs;

    @Mock
    SqsConsumer consumer;

    @Mock
    ThreadMonitor monitor;

    SqsReader reader;

    @Before
    public void setUp (){
        reader = new SqsReader("input-queue", sqs, consumer, monitor);
    }

    @Test
    public void shouldProcessMessage() throws Exception {
        // Given
        final Message msg1 = createSqsMessage("1", "m1");
        final Message msg2 = createSqsMessage("2", "m2");
        final Message msg3 = createSqsMessage("3", "m3");
        final ReceiveMessageResult response = createSqsReceiveMessageResult(msg1, msg2, msg3);
        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
        ;

        // When
        final int result = reader.process();

        // Then
        assertThat(result).isEqualTo(3);

        verify(consumer).consume("m1");
        verify(consumer).consume("m2");
        verify(consumer).consume("m3");

        verify(sqs).deleteMessage("input-queue", "1");
        verify(sqs).deleteMessage("input-queue", "2");
        verify(sqs).deleteMessage("input-queue", "3");
    }

    @Test
    public void shouldNotStopProcessingOnException() throws Exception {
        // Given
        final Message msg1 = createSqsMessage("1", "m1");
        final Message msg2 = createSqsMessage("2", "m2");
        final Message msg3 = createSqsMessage("3", "m3");
        final ReceiveMessageResult response = createSqsReceiveMessageResult(msg1, msg2, msg3);
        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
        ;

        doThrow(IllegalStateException.class).when(consumer).consume("m1");

        // When
        final int result = reader.process();

        // Then
        assertThat(result).isEqualTo(2);

        verify(consumer).consume("m2");
        verify(consumer).consume("m3");

        verify(sqs, never()).deleteMessage("input-queue", "1");
        verify(sqs).deleteMessage("input-queue", "2");
        verify(sqs).deleteMessage("input-queue", "3");

    }
}
