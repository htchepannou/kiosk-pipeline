package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.content.ContentExtractor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@ConfigurationProperties("kiosk.pipeline.ContentExtractorConsumer")
@Transactional
public class ContentExtractorConsumer implements SqsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentExtractorConsumer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    ContentExtractor extractor;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ObjectMapper objectMapper;

    private String inputQueue;
    private String s3Bucket;
    private String s3Key;
    private String s3KeyHtml;

    //-- SqsConsumer
    @Override
    public void consume(final String body) throws IOException {
        final Map snsNotification = (Map) objectMapper.readValue(body, Object.class);
        final Object message = snsNotification.get("Message");
        if (message == null) {
            return;
        }

        final long id = Long.parseLong(message.toString());
        final Link link = linkRepository.findOne(id);
        if (link == null) {
            return;
        }

        consume(link);
    }

    protected void consume(final Link link) throws IOException {
        LOGGER.info("Extracting content from s3://{}/{}", s3Bucket, link.getS3Key());

        final String key = contentKey(link);
        try (final S3Object s3Object = s3.getObject(s3Bucket, link.getS3Key())) {
            final String html = IOUtils.toString(s3Object.getObjectContent());
            final String xhtml = extractor.extract(html);

            final InputStream in = new ByteArrayInputStream(xhtml.getBytes("utf-8"));
            final ObjectMetadata meta = createObjectMetadata(xhtml);

            s3.putObject(s3Bucket, key, in, meta);

            createArticle(link, key);
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
        return metadata;
    }

    private void createArticle(final Link link, final String s3Key) {
        final Article article = new Article();
        article.setLink(link);
        article.setS3Key(s3Key);
        articleRepository.save(article);
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
}
