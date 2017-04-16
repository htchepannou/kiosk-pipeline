package io.tchepannou.kiosk.core.service.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import io.tchepannou.kiosk.core.service.FileRepository;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.MimetypesFileTypeMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class S3FileRepository implements FileRepository {
    @Autowired
    AmazonS3 s3;

    private String bucket;

    @Override
    public void read(final String path, final OutputStream out) throws IOException {
        try (S3Object obj = s3.getObject(bucket, path)) {
            IOUtils.copy(obj.getObjectContent(), out);
        } catch (final AmazonClientException e) {
            throw new IOException(String.format("Unable to read from s3://%s/%s", bucket, path), e);
        }
    }

    @Override
    public void write(final String path, final InputStream in) throws IOException {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            IOUtils.copy(in, out);
            final byte[] bytes = out.toByteArray();

            final ObjectMetadata meta = createObjectMetadata(path, bytes.length);
            s3.putObject(bucket, path, new ByteArrayInputStream(bytes), meta);
        } catch (final AmazonClientException e) {
            throw new IOException(String.format("Unable to write to s3://%s/%s", bucket, path), e);
        }
    }

    @Override
    public void delete(final String path) throws IOException {
        try{
            s3.deleteObject(bucket, path);
        } catch (AmazonClientException e){
            throw new IOException(String.format("Unable to delete s3://%s/%s", bucket, path), e);
        }
    }

    private ObjectMetadata createObjectMetadata(final String path, final int len) {
        final ObjectMetadata meta = new ObjectMetadata();

        final String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(path);
        meta.setContentLength(len);
        if (contentType != null) {
            meta.setContentType(contentType);
        }

        return meta;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }
}
