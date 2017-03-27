package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;

import java.util.List;

@Deprecated
public class TitleSanitizer {
    private final List<TitleFilter> filters;

    public TitleSanitizer(final List<TitleFilter> filters) {
        this.filters = filters;
    }

    public String filter(final Article article) {
        String title = article.getTitle();
        if (title == null) {
            return null;
        }

        final Feed feed = article.getLink().getFeed();
        for (final TitleFilter filter : filters) {
            title = filter.filter(title, feed);
        }
        return title;
    }
}
