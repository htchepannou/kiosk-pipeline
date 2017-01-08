package io.tchepannou.kiosk.pipeline.support;

public class HtmlHelper {
    public static final String[] PUBLISHED_DATE_CSS_SELECTORS = new String[]{
            "article:published_time",
            "shareaholic:article_published_time"
    };

    public static final String[] TITLE_CSS_SELECTORS = new String[]{
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

    public static final String CACHE_CONTROL_CACHE_FOR_30_DAYS = "public, max-age=2592000";
}
