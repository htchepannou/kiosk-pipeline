package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.service.title.TitleSanitizer;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.transaction.Transactional;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static io.tchepannou.kiosk.pipeline.support.JsoupHelper.select;
import static io.tchepannou.kiosk.pipeline.support.JsoupHelper.selectMeta;

@Transactional
@ConfigurationProperties("kiosk.pipeline.ArticleMetadataConsumer")
public class ArticleMetadataConsumer implements SqsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleMetadataConsumer.class);

    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";

    private final String[] TITLE_CSS_SELECTORS = new String[]{
            "article header h1",
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

    private final String[] PUBLISHED_DATE_CSS_SELECTORS = new String[]{
            "article:published_time",
            "og:updated_time",
            "shareaholic:article_published_time"
    };

    @Autowired
    AmazonSQS sqs;

    @Autowired
    AmazonS3 s3;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    TitleSanitizer titleSanitizer;

    private String inputQueue;
    private String outputQueue;
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
            article.setDisplayTitle(titleSanitizer.filter(article));
            setSummary(doc, article);
            setPublishedDate(doc, article);

            articleRepository.save(article);

            sqs.sendMessage(outputQueue, body);
        }
    }

    //-- Private
    private void setSummary(final Document doc, final Article article) {
        final String summary = selectMeta(doc, "meta[property=og:description]");
        if (!Strings.isNullOrEmpty(summary)) {
            article.setSummary(Article.normalizeSummary(summary));
        }
    }

    private void setPublishedDate(final Document doc, final Article article) {
        final DateFormat fmt = new SimpleDateFormat(DATETIME_FORMAT);
        for (final String property : PUBLISHED_DATE_CSS_SELECTORS) {
            final String date = selectMeta(doc, "meta[property=" + property + "]");
            if (!Strings.isNullOrEmpty(date)) {
                try {
                    article.setPublishedDate(fmt.parse(date));
                    break;
                } catch (final Exception e) {
                    LOGGER.warn("Invalid format for published date: {} - Excepting {}", date, DATETIME_FORMAT, e);
                }
            }
        }
    }

    @VisibleForTesting
    protected String extractTitle(final Document doc) {
        String title = selectMeta(doc, "meta[property=og:title]");
        if (title == null) {
            for (final String selector : TITLE_CSS_SELECTORS) {
                title = select(doc, selector);
                if (title != null) {
                    break;
                }
            }
        }
        return title;
    }

    //-- Getter/Setter
    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }

    public String getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(final String outputQueue) {
        this.outputQueue = outputQueue;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(final String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }
}
