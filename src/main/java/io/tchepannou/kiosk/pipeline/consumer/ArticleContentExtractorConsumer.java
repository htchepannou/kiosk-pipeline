package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.service.content.ContentExtractor;
import io.tchepannou.kiosk.pipeline.support.HtmlHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Transactional
public class ArticleContentExtractorConsumer implements SqsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleContentExtractorConsumer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    AmazonSQS sqs;

    @Autowired
    ContentExtractor extractor;

    @Autowired
    ArticleRepository articleRepository;

    private String inputQueue;
    private String outputQueue;
    private String s3Bucket;
    private String s3Key;
    private String s3KeyHtml;

    //-- SqsConsumer
    @Override
    public void consume(final String body) throws IOException {
        if (!StringUtils.isNumeric(body)) {
            LOGGER.info("Invalid message. Expecting a number and got {}", body);
            return;
        }
        final long id = Long.parseLong(body.toString());
        final Article article = articleRepository.findOne(id);
        consume(article);
    }

    protected void consume(final Article article) throws IOException {
        final Link link = article.getLink();
        LOGGER.info("Extracting content from s3://{}/{}", s3Bucket, link.getS3Key());

        final String key = contentKey(link);
        try (final S3Object s3Object = s3.getObject(s3Bucket, link.getS3Key())) {
            final String html = IOUtils.toString(s3Object.getObjectContent(), "utf-8");
            final String xhtml = extractor.extract(html);

            final byte[] bytes = xhtml.getBytes("utf-8");
            final InputStream in = new ByteArrayInputStream(bytes);
            final ObjectMetadata meta = createObjectMetadata(xhtml);

            // Storing to S3
            LOGGER.info("Storing content of {} to s3://{}/{}", link.getUrl(), s3Bucket, key);
            s3.putObject(s3Bucket, key, in, meta);

            // Updating
            LOGGER.info("Updating {} in the DB", link.getUrl());
            article.setS3Key(key);
            article.setContentLength(bytes.length);
            articleRepository.save(article);

            LOGGER.info("Sending message <{}> to {}", article.getId(), outputQueue);
            sqs.sendMessage(outputQueue, String.valueOf(article.getId()));
        }
    }

    private String contentKey(final Link link) {
        final String key = link.getS3Key();
        return s3Key + key.substring(s3KeyHtml.length());
    }

    private ObjectMetadata createObjectMetadata(final String html) {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/html");
        metadata.setContentLength(html.length());
        metadata.setCacheControl(HtmlHelper.CACHE_CONTROL_CACHE_FOR_30_DAYS);
        return metadata;
    }

    //-- Getter/Setter
    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }

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

    public String getS3KeyHtml() {
        return s3KeyHtml;
    }

    public void setS3KeyHtml(final String s3KeyHtml) {
        this.s3KeyHtml = s3KeyHtml;
    }

    public String getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(final String outputQueue) {
        this.outputQueue = outputQueue;
    }
}
