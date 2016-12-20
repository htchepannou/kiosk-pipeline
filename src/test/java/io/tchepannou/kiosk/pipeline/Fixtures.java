package io.tchepannou.kiosk.pipeline;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import io.tchepannou.kiosk.pipeline.model.Feed;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Fixtures {
    public static ReceiveMessageResult createSqsReceiveMessageResult(final Message... messages) {
        final ReceiveMessageResult result = mock(ReceiveMessageResult.class);
        when(result.getMessages()).thenReturn(Arrays.asList(messages));
        return result;
    }

    public static Message createSqsMessage(final String handle, final String body) {
        final Message msg = mock(Message.class);
        when(msg.getReceiptHandle()).thenReturn(handle);
        when(msg.getBody()).thenReturn(body);
        return msg;
    }

    public static Feed createFeed(final String name, final String url, final String path){
        final Feed feed = new Feed();
        feed.setUrl(url);
        feed.setPath(path);
        feed.setName(name);
        return feed;
    }
}
