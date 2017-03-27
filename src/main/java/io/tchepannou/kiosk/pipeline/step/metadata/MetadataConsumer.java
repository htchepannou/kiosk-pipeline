package io.tchepannou.kiosk.pipeline.step.metadata;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.step.LinkConsumer;
import io.tchepannou.kiosk.pipeline.support.HtmlHelper;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.transaction.Transactional;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;

import static io.tchepannou.kiosk.pipeline.support.JsoupHelper.select;
import static io.tchepannou.kiosk.pipeline.support.JsoupHelper.selectMeta;

@Transactional
public class MetadataConsumer extends LinkConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataConsumer.class);

    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    @Qualifier("ContentMessageQueue")
    MessageQueue queue;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    Clock clock;

    @Autowired
    TitleFilter titleFilter;

    private int defaultPublishDateOffsetDays = -7;

    @Override
    protected void consume(final Link link) throws IOException {
        final Document doc = getRawDocument(link);

        updateLink(link, doc);

        push(link, queue);
    }

    @VisibleForTesting
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

    private void updateLink(final Link link, final Document doc) {
        final String title = extractTitle(doc);
        final Feed feed = link.getFeed();

        link.setTitle(extractTitle(doc));
        link.setSummary(extractSummary(doc));
        link.setPublishedDate(extractPublishedDate(doc, link.getFeed()));
        link.setDisplayTitle(titleFilter.filter(title, feed));
        link.setType(extractType(doc));

        linkRepository.save(link);
    }

    private Date asDate(final String date, final DateFormat fmt) {
        try {
            return fmt.parse(date);
        } catch (final Exception e) {
            LOGGER.warn("Invalid format for published date: {} - Excepting {}", date, DATETIME_FORMAT, e);
            return null;
        }
    }

    private String extractType(final Document doc) {
        final String type = selectMeta(doc, "meta[property=og:type]");
        if (Strings.isNullOrEmpty(type)){
            return LinkTypeEnum.article.name();
        } else {
            return type.toLowerCase();
        }
    }

    public int getDefaultPublishDateOffsetDays() {
        return defaultPublishDateOffsetDays;
    }

    public void setDefaultPublishDateOffsetDays(final int defaultPublishDateOffsetDays) {
        this.defaultPublishDateOffsetDays = defaultPublishDateOffsetDays;
    }
}
