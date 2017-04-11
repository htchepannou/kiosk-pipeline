package io.tchepannou.kiosk.core.service.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import io.tchepannou.kiosk.pipeline.Fixtures;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class S3FileRepositoryTest {

    @Mock
    AmazonS3 s3;

    @InjectMocks
    S3FileRepository repo;

    @Before
    public void setUp () {
        repo.setBucket("bucket");
    }

    @Test
    public void testRead() throws Exception {
        // Given
        final S3Object obj = Fixtures.createS3Object("bucket", "/testRead/01.txt", "toto");
        when(s3.getObject("bucket", "testRead/01.txt")).thenReturn(obj);

        // When
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        repo.read("testRead/01.txt", out);

        // Then
        assertThat(out.toString()).isEqualTo("toto");

    }

    @Test(expected = IOException.class)
    public void testReadRetrowsAwsExceptionAsIOException() throws Exception {
        // Given
        final AmazonClientException ex = new AmazonClientException("failed");
        when(s3.getObject("bucket", "testRead/01.txt")).thenThrow(ex);

        // When
        repo.read("testRead/01.txt", new ByteArrayOutputStream());
    }


    @Test
    public void testWrite() throws Exception {
        // Given
        final ByteArrayInputStream in = new ByteArrayInputStream("toto".getBytes());

        // When
        repo.write("testWrite/01.txt", in);

        // Then
        ArgumentCaptor<ObjectMetadata> meta = ArgumentCaptor.forClass(ObjectMetadata.class);
        ArgumentCaptor<InputStream> xin = ArgumentCaptor.forClass(InputStream.class);
        verify(s3).putObject(eq("bucket"), eq("testWrite/01.txt"), xin.capture(), meta.capture());

        assertThat(IOUtils.toString(xin.getValue())).isEqualTo("toto");
    }

    @Test(expected = IOException.class)
    public void testWriteRetrowsAwsExceptionAsIOException() throws Exception {
        // Given
        final AmazonClientException ex = new AmazonClientException("failed");
        when(s3.putObject(any(), any(), any(), any())).thenThrow(ex);

        final ByteArrayInputStream in = new ByteArrayInputStream("toto".getBytes());

        // When
        repo.write("testWrite/01.txt", in);
    }

    @Test
    public void testDelete() throws Exception {
        // When
        repo.delete("testDelete/01.txt");

        // Then
        verify(s3).deleteObject("bucket", "testDelete/01.txt");
    }
}
