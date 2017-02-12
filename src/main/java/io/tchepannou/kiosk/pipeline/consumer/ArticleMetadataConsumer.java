package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsSnsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.title.TitleSanitizer;
import io.tchepannou.kiosk.pipeline.support.HtmlHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;

import static io.tchepannou.kiosk.pipeline.support.JsoupHelper.select;
import static io.tchepannou.kiosk.pipeline.support.JsoupHelper.selectMeta;

@Transactional
public class ArticleMetadataConsumer extends SqsSnsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleMetadataConsumer.class);

    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    AmazonSQS sqs;

    @Autowired
    AmazonS3 s3;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    TitleSanitizer titleSanitizer;

    @Autowired
    Clock clock;

    private String inputQueue;
    private String outputQueue;
    private String s3Bucket;
    private int defaultPublishDateOffsetDays = -7;

    @Override
    public void consumeMessage(final String body) throws IOException {
        final long id = Long.parseLong(body);
        final Link link = linkRepository.findOne(id);
        final Feed feed = link.getFeed();

        LOGGER.info("Extracting metadata from {}", link.getUrl());
        try (final S3Object s3Object = s3.getObject(s3Bucket, link.getS3Key())) {
            final String html = IOUtils.toString(s3Object.getObjectContent(), "utf-8");
            final Document doc = Jsoup.parse(html);

            final Article article = new Article();
            article.setLink(link);
            article.setTitle(extractTitle(doc));
            article.setSummary(extractSummary(doc));
            article.setPublishedDate(extractPublishedDate(doc, feed));
            article.setDisplayTitle(titleSanitizer.filter(article));
            article.setType(extractType(doc));

            articleRepository.save(article);

            LOGGER.info("Sending message <{}> to {}", article.getId(), outputQueue);
            sqs.sendMessage(outputQueue, String.valueOf(article.getId()));
        }
    }

    //-- Private
    private String extractType(final Document doc) {
        if (selectMeta(doc, "meta[property=og:title]") != null) {
            // This document supports OG
            return selectMeta(doc, "meta[property=og:type]");
        } else {
            return Article.TYPE_ARTICLE;
        }
    }

    protected String extractSummary(final Document doc) {
        final String summary = selectMeta(doc, "meta[property=og:description]");
        return summary != null ? Article.normalizeSummary(summary) : null;
    }

    @VisibleForTesting
    protected Date extractPublishedDate(final Document doc, final Feed feed) {
        final DateFormat fmt = new SimpleDateFormat(DATETIME_FORMAT);
        Date result = null;
        for (final String property : HtmlHelper.META_PUBLISHED_DATE_CSS_SELECTORS) {

            final String date = property.startsWith("shareaholic")
                    ? selectMeta(doc, "meta[name=" + property + "]")
                    : selectMeta(doc, "meta[property=" + property + "]");
            if (!Strings.isNullOrEmpty(date)) {
                result = asDate(date, fmt);
                if (result != null) {
                    break;
                }
            }
        }

        if (result == null) {
            for (final String property : HtmlHelper.TIME_PUBLISHED_DATE_CSS_SELECTORS) {
                final Elements elts = doc.select(property);
                if (elts.size() > 0) {
                    final Element elt = elts.get(0);
                    String date = elt.attr("datetime");
                    if (Strings.isNullOrEmpty(date)) {
                        date = elt.attr("title");
                    }
                    if (date != null) {
                        result = asDate(date, fmt);
                    }
                }

            }
        }

        if (result != null) {
            return result;
        } else {
            final Date now = new Date(clock.millis());
            final Date onboardDate = feed.getOnboardDate();
            final DateFormat fmt2 = new SimpleDateFormat(DATE_FORMAT);
            if (fmt2.format(now).equals(fmt2.format(onboardDate))) {
                return DateUtils.addDays(now, defaultPublishDateOffsetDays);
            }
            return now;
        }
    }

    private Date asDate(final String date, final DateFormat fmt) {
        try {
            return fmt.parse(date);
        } catch (final Exception e) {
            LOGGER.warn("Invalid format for published date: {} - Excepting {}", date, DATETIME_FORMAT, e);
            return null;
        }
    }

    @VisibleForTesting
    protected String extractTitle(final Document doc) {
        String title = selectMeta(doc, "meta[property=og:title]");
        if (title == null) {
            for (final String selector : HtmlHelper.TITLE_CSS_SELECTORS) {
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

    public int getDefaultPublishDateOffsetDays() {
        return defaultPublishDateOffsetDays;
    }

    public void setDefaultPublishDateOffsetDays(final int defaultPublishDateOffsetDays) {
        this.defaultPublishDateOffsetDays = defaultPublishDateOffsetDays;
    }
}
