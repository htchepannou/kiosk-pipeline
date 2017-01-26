package io.tchepannou.kiosk.pipeline.support;

import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.List;

public class HtmlHelper {
    public static final List<String> TAG_HEADING = Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6");

    public static final String[] META_PUBLISHED_DATE_CSS_SELECTORS = new String[]{
            "article:published_time",
            "shareaholic:article_published_time"
    };

    public static final String[] TIME_PUBLISHED_DATE_CSS_SELECTORS = new String[]{
            "time[itemprop=dateCreated]",
            "time.entry-date",
            "abbr[itemprop~=datePublished]",
    };

    public static final String[] TITLE_CSS_SELECTORS = new String[]{
            "article header h1",
            "article h1",
            ".entry-content h1",
            ".entry-title",
            ".post-title",
            ".pageTitle",
            ".page-title",
            "#page-title",
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

    public static final String CACHE_CONTROL_CACHE_FOR_30_DAYS = "public, max-age=2592000";

    public static boolean isHeading(final Element elt) {
        return TAG_HEADING.contains(elt.tagName().toLowerCase());
    }
}
