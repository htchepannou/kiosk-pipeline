package io.tchepannou.kiosk.pipeline.service.title;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;

public class TitleSuffixFilter implements TitleFilter {
    private static final String TRIM = ":|- ";

    @Override
    public String filter(final String title, final Article article) {
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
