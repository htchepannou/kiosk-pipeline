package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;

@ConfigurationProperties("kiosk.pipeline.HtmlDownloadConsumer")
public class HtmlDownloadConsumer implements SqsConsumer {
    public static final Logger LOGGER = LoggerFactory.getLogger(HtmlDownloadConsumer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    HttpService http;

    @Autowired
    Clock clock;

    private String outputS3Bucket;
    private String outputS3Key;

    @Override
    public void consume(final String body) throws IOException {
        // Download
        LOGGER.info("Downloading {}", body);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        http.get(body, out);

        // Store
        final String id = DigestUtils.md5Hex(body);
        final String key = generateKey(id);
        final byte[] bytes = out.toByteArray();
        final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        final ObjectMetadata meta = createObjectMetadata(bytes.length);

        LOGGER.info("Storing {} to s3://{}/{}", body, outputS3Bucket, key);
        s3.putObject(outputS3Bucket, key, in, meta);
    }


    //-- Private
    private String generateKey(final String id) {
        final DateFormat fmt = new SimpleDateFormat("yyyy/MM/dd/HH");
        final Date now = new Date(clock.millis());
        return String.format("%s/%s/%s.html", outputS3Key, fmt.format(now), id);
    }

    private ObjectMetadata createObjectMetadata(int len){
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/html");
        metadata.setContentLength(len);
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
}
