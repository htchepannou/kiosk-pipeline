package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;

import java.util.List;

public class TitleSanitizer {
    private final List<TitleFilter> filters;

    public TitleSanitizer(final List<TitleFilter> filters) {
        this.filters = filters;
    }

    public String filter(final Article article) {
        String title = article.getTitle();
        if (title != null) {
            for (final TitleFilter filter : filters) {
                title = filter.filter(title, article);
            }
        }
        return title;
    }
}
