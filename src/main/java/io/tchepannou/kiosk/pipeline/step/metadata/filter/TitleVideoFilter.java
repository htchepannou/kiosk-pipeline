package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.core.nlp.filter.UnaccentTextFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;

public class TitleVideoFilter implements TitleFilter {
    private final String[] TOKENS = new String[]{
            "(video)",
            "( video )",
            "[video]",
            "[ video ]",
            "[►]",
            "[ ► ]"
    };

    final UnaccentTextFilter unaccentTextFilter = new UnaccentTextFilter();

    @Override
    public String filter(final String title, final Feed feed) {
        final String xtitle = unaccentTextFilter.filter(title.toLowerCase());
        for (final String token : TOKENS) {
            if (xtitle.endsWith(token)) {
                return title.substring(0, title.length() - token.length()).trim();
            } else if (xtitle.startsWith(token)) {
                return title.substring(token.length()).trim();
            }
        }
        return title;
    }
}
