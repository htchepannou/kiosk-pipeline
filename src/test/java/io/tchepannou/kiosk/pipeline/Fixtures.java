package io.tchepannou.kiosk.pipeline;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.persistence.domain.Tag;
import org.apache.commons.io.IOUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Fixtures {
    private static long uuid = System.currentTimeMillis();
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

    public static Feed createFeed() {
        final Feed feed = new Feed();
        feed.setId(++uuid);
        feed.setUrl("http://ffed.com/" + uuid);
        feed.setName("feed_" + uuid);
        feed.setOnboardDate(new Date());
        feed.setActive(true);
        return feed;

    }
    public static Feed createFeed(final String name, final String url, final String path) {
        final Feed feed = new Feed();
        feed.setId(++uuid);
        feed.setUrl(url);
        feed.setPath(path);
        feed.setName(name);
        feed.setOnboardDate(new Date());
        return feed;
    }

    public static S3Object createS3Object(final String bucket, final String key, final String content) throws IOException {
        final S3Object obj = mock(S3Object.class);
        when(obj.getBucketName()).thenReturn(bucket);
        when(obj.getKey()).thenReturn(key);

        final S3ObjectInputStream in = createS3InputStream(content);
        when(obj.getObjectContent()).thenReturn(in);

        return obj;
    }
    public static S3ObjectInputStream createS3InputStream(final String content) throws IOException {
        final S3ObjectInputStream in = mock(S3ObjectInputStream.class);
        when(in.read(any(byte[].class), anyInt(), anyInt())).then(new Answer<Integer>() {
            @Override
            public Integer answer(final InvocationOnMock inv) throws Throwable {
                final byte[] buff = (byte[]) inv.getArguments()[0];
                return read(content, buff);
            }
        });

        when(in.read(any(byte[].class))).then(new Answer<Integer>() {
            @Override
            public Integer answer(final InvocationOnMock inv) throws Throwable {
                final byte[] buff = (byte[]) inv.getArguments()[0];
                return read(content, buff);
            }
        });

        return in;
    }

    public static Link createLink(){
        final Link link = new Link();
        link.setId(++uuid);
        link.setUrl("http://gooo.com/" + uuid);
        link.setFeed(createFeed("Test Feed", "http://kiosk.com/test", null));
        return link;
    }

    public static Tag createTag(final String name){
        final Tag tag = new Tag();
        tag.setId(++uuid);
        tag.setName(name);
        return tag;
    }

    public static int read(final String content, final byte[] buff){
        if (++s3Read % 2 == 0) {
            return -1;
        }

        for (int i = 0; i < content.length(); i++) {
            buff[i] = content.getBytes()[i];
        }
        return content.length();
    }

    public static Answer readText(final String txt) {
        return (inv) -> {
            final OutputStream out = (OutputStream) inv.getArguments()[1];
            IOUtils.copy(new ByteArrayInputStream(txt.getBytes()), out);
            return null;
        };
    }

}
