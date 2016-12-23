package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.tchepannou.kiosk.pipeline.service.extractor.ContentExtractor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentExtractorConsumerTest {
    @Mock
    AmazonS3 s3;

    @Mock
    ContentExtractor extractor;

    @InjectMocks
    ContentExtractorConsumer consumer;

    int read;

    @Before
    public void setUp() {
        consumer.setHtmlS3Key("dev/html");
        consumer.setOutputS3Bucket("bucket");
        consumer.setOutputS3Key("dev/content");

        read = 1;
    }

    @Test
    public void shouldConsume() throws Exception {
        // Given
        final S3Object obj = createS3Object("bucket", "dev/html/2010/10/11/test.html");
        final S3ObjectInputStream in = createS3InputStream("hello");
        when(obj.getObjectContent()).thenReturn(in);

        when(extractor.extract("hello")).thenReturn("world");

        // When
        consumer.consume(obj);

        // Then
        verify(s3).putObject(
                eq("bucket"),
                eq("dev/content/2010/10/11/test.html"),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );
    }

    private S3Object createS3Object(final String bucket, final String key) throws Exception {
        final S3Object obj = mock(S3Object.class);
        when(obj.getBucketName()).thenReturn(bucket);
        when(obj.getKey()).thenReturn(key);

        return obj;
    }

    private S3ObjectInputStream createS3InputStream(final String content) throws IOException {
        final S3ObjectInputStream in = mock(S3ObjectInputStream.class);
        when(in.read(any(byte[].class), anyInt(), anyInt())).then(new Answer<Integer>() {
            @Override
            public Integer answer(final InvocationOnMock inv) throws Throwable {
                final byte[] buff = (byte[]) inv.getArguments()[0];
                if (read-- <= 0){
                    return -1;
                }
                for (int i=0 ; i<content.length() ; i++){
                    buff[i] = content.getBytes()[i];
                }
                return content.length();
            }
        });
        return in;
    }
}
