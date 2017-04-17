package io.tchepannou.kiosk.pipeline.step.metadata.filter;

import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;

public class TitleSuffixFilter implements TitleFilter {
    private static final String TRIM = ":|- ";

    @Override
    public String filter(final String title, final Feed feed) {
        final String xtitle = title.trim();
        int i = xtitle.length() - 1;

        for (; i >= 0; i--) {
            if (!shouldTrim(xtitle.charAt(i))) {
                break;
            }
        }

        return title.substring(0, i + 1);
    }

    private boolean shouldTrim(final char ch) {
        return TRIM.indexOf(ch) >= 0;
    }
}
