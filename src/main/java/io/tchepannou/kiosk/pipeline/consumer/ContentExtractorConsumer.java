package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsS3Consumer;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.content.ContentExtractor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@ConfigurationProperties("kiosk.pipeline.ContentExtractorConsumer")
public class ContentExtractorConsumer extends SqsS3Consumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentExtractorConsumer.class);

    @Autowired
    ContentExtractor extractor;

    @Autowired
    LinkRepository linkRepository;

    private String inputQueue;
    private String outputS3Bucket;
    private String outputS3Key;
    private String htmlS3Key;

    //-- SqsConsumer

    @Override
    protected void consume(final S3Object s3Object) throws IOException {
        LOGGER.info("Extracting content from s3://{}/{}", s3Object.getBucketName(), s3Object.getKey());

        final String key = contentKey(s3Object);
        if (key == null) {
            LOGGER.error("s3://{}/{} is not originating from valid location: {}", s3Object.getBucketName(), s3Object.getKey(), htmlS3Key);
            return;
        }

        final String html = IOUtils.toString(s3Object.getObjectContent());
        final String xhtml = extractor.extract(html);

        final InputStream in = new ByteArrayInputStream(xhtml.getBytes("utf-8"));
        final ObjectMetadata meta = createObjectMetadata(xhtml);

        s3.putObject(outputS3Bucket, key, in, meta);
    }

    @Override
    protected void onException(final S3EventNotification.S3EventNotificationRecord record, final Throwable e) {
        LOGGER.error("Unecpected error when processing s3://{}/{}",
                getBucket(record),
                getKey(record),
                e);
    }

    private String contentKey(final S3Object s3Object) {
        final String key = s3Object.getKey();
        return key.startsWith(htmlS3Key)
                ? outputS3Key + key.substring(htmlS3Key.length())
                : null;
    }

    private ObjectMetadata createObjectMetadata(final String html) {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/html");
        metadata.setContentLength(html.length());
        return metadata;
    }

    //-- Getter/Setter
    public String getOutputS3Bucket() {
        return outputS3Bucket;
    }

    public void setOutputS3Bucket(final String outputS3Bucket) {
        this.outputS3Bucket = outputS3Bucket;
    }

    public String getOutputS3Key() {
        return outputS3Key;
    }

    public void setOutputS3Key(final String outputS3Key) {
        this.outputS3Key = outputS3Key;
    }

    public String getHtmlS3Key() {
        return htmlS3Key;
    }

    public void setHtmlS3Key(final String htmlS3Key) {
        this.htmlS3Key = htmlS3Key;
    }

    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }
}
