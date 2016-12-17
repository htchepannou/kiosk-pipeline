package io.tchepannou.kiosk.pipeline.processor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tchepannou.kiosk.pipeline.model.Feed;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadFeedsProcessorTest {
    private static final String MSG1 = "{\n"
            + "  \"name\": \"cameroon-info.net\",\n"
            + "  \"url\": \"http://www.cameroon-info.net\",\n"
            + "  \"path\": \"/article/*.html\"\n"
            + "}\n";

    private static final String MSG2 = "{\n"
            + "  \"name\": \"camfoot\",\n"
            + "  \"url\": \"http://www.camfoot.com\",\n"
            + "  \"path\": \"*.html\"\n"
            + "}\n";

    @Mock
    AmazonS3 s3;

    @Mock
    AmazonSQS sqs;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    LoadFeedsProcessor processor;

    @Before
    public void setUp() {
        processor.setBucket("bucket");
        processor.setKey("feeds");
        processor.setQueue("feed-queue");
    }

    @Test
    public void shouldProcess() throws Exception {
        // Given
        final S3ObjectSummary s1 = createS3ObjectSummary("bucket", "feeds/f1.json");
        final S3ObjectSummary s2 = createS3ObjectSummary("bucket", "feeds/f2.json");
        final ObjectListing lst = createS3ObjectListing(s1, s2);
        when(s3.listObjects("bucket", "feeds")).thenReturn(lst);

        final S3Object o1 = createS3Object();
        final S3Object o2 = createS3Object();
        when(s3.getObject("bucket", "feeds/f1.json")).thenReturn(o1);
        when(s3.getObject("bucket", "feeds/f2.json")).thenReturn(o2);

        final Feed f1 = new Feed();
        final Feed f2 = new Feed();
        when(objectMapper.readValue(any(InputStream.class), any(Class.class)))
                .thenReturn(f1)
                .thenReturn(f2);
        when(objectMapper.writeValueAsString(any()))
                .thenReturn(MSG1)
                .thenReturn(MSG2);


        // When
        processor.process();

        // Then
        verify(sqs).sendMessage("feed-queue", MSG1);
        verify(sqs).sendMessage("feed-queue", MSG2);
    }

    private ObjectListing createS3ObjectListing(final S3ObjectSummary... summaries) {
        final ObjectListing lst = mock(ObjectListing.class);
        when(lst.getObjectSummaries()).thenReturn(Arrays.asList(summaries));
        return lst;
    }

    private S3ObjectSummary createS3ObjectSummary(final String bucket, final String key) {
        final S3ObjectSummary summary = mock(S3ObjectSummary.class);
        when(summary.getBucketName()).thenReturn(bucket);
        when(summary.getKey()).thenReturn(key);
        return summary;
    }

    private S3Object createS3Object() {
        final S3Object obj = mock(S3Object.class);
        final S3ObjectInputStream in = mock(S3ObjectInputStream.class);
        when(obj.getObjectContent()).thenReturn(in);
        return obj;
    }
}
