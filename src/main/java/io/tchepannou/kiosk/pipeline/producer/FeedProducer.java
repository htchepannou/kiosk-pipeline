package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;

@ConfigurationProperties("kiosk.pipeline.FeedProducer")
public class FeedProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedProducer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    AmazonSQS sqs;

    @Autowired
    ObjectMapper objectMapper;

    private String s3Bucket;
    private String s3Key;
    private String outputQueue;

    public void produce(){
        final ObjectListing list = s3.listObjects(s3Bucket, s3Key);
        for (final S3ObjectSummary summary : list.getObjectSummaries()){
            final String key = summary.getKey();
            if (!key.endsWith(".json")){
                continue;
            }

            try {
                process(summary);
            } catch (Exception e){
                LOGGER.error("Unable to process {}/{}", s3Bucket, key, e);
            }
        }
    }

    private void process(final S3ObjectSummary summary) throws IOException {
        final String bucket = summary.getBucketName();
        final String key = summary.getKey();
        LOGGER.info("Processing {}/{}", bucket, key);

        try(final S3Object obj = s3.getObject(bucket, key)){
            final Feed feed = objectMapper.readValue(obj.getObjectContent(), Feed.class);
            sqs.sendMessage(outputQueue, objectMapper.writeValueAsString(feed));
        }
    }

    //-- Getter/Setter
    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(final String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(final String s3Key) {
        this.s3Key = s3Key;
    }

    public String getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(final String outputQueue) {
        this.outputQueue = outputQueue;
    }
}
