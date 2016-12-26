package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.transaction.Transactional;
import java.io.IOException;

@Transactional
@ConfigurationProperties("kiosk.pipeline.MetadataExtractorConsumer")
public class MetadataExtractorConsumer implements SqsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataExtractorConsumer.class);

    private final String[] CSS_SELECTORS = new String[]{
            "article h1",
            ".entry-content h1",
            ".entry-title",
            ".post-title",
            ".pageTitle",
            ".post_title",
            ".headline h1",
            ".headline",
            ".story h1",
            ".entry-header h1",
            ".news_title",
            "#page-post h1",
            ".postheader h1",
            ".postheader h2",
            ".type-post h1",
            ".instapaper_title",
            ".markdown-body h1",
    };

    @Autowired
    AmazonS3 s3;

    @Autowired
    ArticleRepository articleRepository;

    private String inputQueue;
    private String s3Bucket;

    @Override
    public void consume(final String body) throws IOException {
        final long id = Long.parseLong(body);
        final Article article = articleRepository.findOne(id);

        LOGGER.info("Extracting title from {}", article.getLink().getUrl());
        try (final S3Object s3Object = s3.getObject(s3Bucket, article.getLink().getS3Key())) {
            final String html = IOUtils.toString(s3Object.getObjectContent());
            final Document doc = Jsoup.parse(html);
            article.setTitle(extractTitle(doc));
            article.setSummary(extractSummary(doc));

            articleRepository.save(article);
        }
    }

    //-- Private
    protected String extractSummary(final Document doc) {
        return selectMeta(doc, "meta[property=og:description]");
    }

    protected String extractTitle(final Document doc) {
        String title = selectMeta(doc, "meta[property=og:title]");
        if (title == null) {
            for (final String selector : CSS_SELECTORS) {
                title = select(doc, selector);
                if (title != null) {
                    break;
                }
            }
        }
        return title;
    }

    private String selectMeta(final Document doc, final String cssSelector) {
        final Elements elts = doc.select(cssSelector);
        return elts.isEmpty() ? null : elts.attr("content");
    }

    private String select(final Document doc, final String cssSelector) {
        final Elements elts = doc.select(cssSelector);
        return elts.isEmpty() ? null : elts.text();
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
}
