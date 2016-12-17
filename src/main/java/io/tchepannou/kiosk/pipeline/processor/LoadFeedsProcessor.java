package io.tchepannou.kiosk.pipeline.processor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tchepannou.kiosk.pipeline.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;

@ConfigurationProperties("kiosk.pipeline.LoadFeedsProcessor")
public class LoadFeedsProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFeedsProcessor.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    AmazonSQS sqs;

    @Autowired
    ObjectMapper objectMapper;

    private String bucket;
    private String key;
    private String queue;

    public void process(){
        final ObjectListing list = s3.listObjects(bucket, key);
        for (final S3ObjectSummary summary : list.getObjectSummaries()){
            final String key = summary.getKey();
            if (!key.endsWith(".json")){
                continue;
            }

            try {
                process(summary);
            } catch (Exception e){
                LOGGER.error("Unable to process {}/{}", bucket, key, e);
            }
        }
    }

    private void process(final S3ObjectSummary summary) throws IOException {
        final String bucket = summary.getBucketName();
        final String key = summary.getKey();
        LOGGER.info("Processing {}/{}", bucket, key);

        try(final S3Object obj = s3.getObject(bucket, key)){
            final Feed feed = objectMapper.readValue(obj.getObjectContent(), Feed.class);
            sqs.sendMessage(queue, objectMapper.writeValueAsString(feed));
        }
    }

    //-- Getter/Setter
    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(final String queue) {
        this.queue = queue;
    }
}
