package io.tchepannou.kiosk.pipeline.step.metadata;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;

import java.util.List;

public class TitleFilterSet implements TitleFilter{
    private final List<TitleFilter> filters;

    public TitleFilterSet(final List<TitleFilter> filters) {
        this.filters = filters;
    }

    @Override
    public String filter(final String title, final Feed feed) {
        String xtitle = title;
        for (final TitleFilter filter : filters){
            xtitle = filter.filter(title, feed);
        }
        return xtitle;
    }
}
