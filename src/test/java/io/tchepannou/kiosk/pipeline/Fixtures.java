package io.tchepannou.kiosk.pipeline;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Fixtures {
    private static int s3Read;

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

    public static Feed createFeed(final String name, final String url, final String path) {
        final Feed feed = new Feed();
        feed.setUrl(url);
        feed.setPath(path);
        feed.setName(name);
        return feed;
    }

    public static S3ObjectInputStream createS3InputStream(final String content) throws IOException {
        final S3ObjectInputStream in = mock(S3ObjectInputStream.class);
        when(in.read(any(byte[].class), anyInt(), anyInt())).then(new Answer<Integer>() {
            @Override
            public Integer answer(final InvocationOnMock inv) throws Throwable {
                if (++s3Read % 2 == 0){
                    return -1;
                }

                final byte[] buff = (byte[]) inv.getArguments()[0];
                for (int i = 0; i < content.length(); i++) {
                    buff[i] = content.getBytes()[i];
                }
                return content.length();
            }
        });
        return in;
    }

}
