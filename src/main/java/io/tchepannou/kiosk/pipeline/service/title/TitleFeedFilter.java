package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;

public class TitleFeedFilter implements TitleFilter {
    @Override
    public String filter(final String title, final Article article) {
        final Feed feed = article.getLink().getFeed();
        final String feedName = feed.getName().toLowerCase();
        final String xtitle = title.toLowerCase();

        return xtitle.endsWith(feedName)
                ? title.substring(0, title.length()-feedName.length())
                : title;
    }
}
