package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;

public class TitleFeedFilter implements TitleFilter {
    @Override
    public String filter(final String title, final Feed feed) {
        final String feedName = feed.getName().toLowerCase();
        final String xtitle = title.toLowerCase();

        return xtitle.endsWith(feedName)
                ? title.substring(0, title.length()-feedName.length())
                : title;
    }
}
