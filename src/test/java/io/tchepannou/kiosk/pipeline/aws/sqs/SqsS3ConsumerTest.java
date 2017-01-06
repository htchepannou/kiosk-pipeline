package io.tchepannou.kiosk.pipeline.aws.sqs;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsS3ConsumerTest {
    @Mock
    AmazonS3 s3;

    @InjectMocks
    ConsumerImpl consumer;

    @Test
    public void shouldConsumeS3Notification() throws Exception {
        final String body = IOUtils.toString(getClass().getResourceAsStream("/aws/s3_notification.json"));

        final S3Object obj = mock(S3Object.class);
        when(s3.getObject("bucket", "foo/bar.txt")).thenReturn(obj);

        // When
        final ConsumerImpl xconsumer = spy(consumer);
        xconsumer.consume(body);

        // Then
        verify(xconsumer).consume(obj);

    }

    @Test
    public void shouldIgnoreEmptyS3Notification() throws Exception {
        final String body = IOUtils.toString(getClass().getResourceAsStream("/aws/s3_notification_empty.json"));

        final S3Object obj = mock(S3Object.class);
        when(s3.getObject("bucket", "foo/bar.txt")).thenReturn(obj);

        // When
        final ConsumerImpl xconsumer = spy(consumer);
        xconsumer.consume(body);

        // Then
        verify(xconsumer, never()).consume(obj);

    }

    public static class ConsumerImpl extends SqsS3Consumer {
        @Override
        protected void consume(final S3Object s3Object) throws IOException {

        }
    }

}
