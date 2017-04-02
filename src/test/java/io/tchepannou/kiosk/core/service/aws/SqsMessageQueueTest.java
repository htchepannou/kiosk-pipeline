package io.tchepannou.kiosk.core.service.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsMessageQueueTest {
    static final String URL = "https://www.amazon.com/sqs/queue";

    @Mock
    AmazonSQS sqs;

    @Mock
    Message msg1;
    @Mock
    Message msg2;
    @Mock
    Message msg3;


    @InjectMocks
    SqsMessageQueue queue;

    @Before
    public void setUp (){
        queue.setUrl(URL);
    }

    @Test
    public void testPush() throws Exception {
        // When
        queue.push("toto");

        // Then
        verify(sqs).sendMessage(URL, "toto");
    }

    @Test
    public void testPoll() throws Exception {
        // Given
        init(msg1, "1", "h1");
        init(msg2, "2", "h2");
        init(msg3, "3", "h3");
        ReceiveMessageResult result = mock(ReceiveMessageResult.class);
        when(result.getMessages()).thenReturn(Arrays.asList(msg1, msg2, msg3));

        when(sqs.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(result);

        // When
        final List<String> value = queue.poll();

        // Then
        assertThat(value).containsExactly("1", "2", "3");

        verify(sqs).deleteMessage(URL, "h1");
        verify(sqs).deleteMessage(URL, "h2");
        verify(sqs).deleteMessage(URL, "h3");
    }

    public void testName () {
        assertThat(queue.getName()).isEqualTo("queue");
    }

    private void init(final Message msg, final String body, final String handle) {
        when(msg.getBody()).thenReturn(body);
        when(msg.getReceiptHandle()).thenReturn(handle);
    }

}
