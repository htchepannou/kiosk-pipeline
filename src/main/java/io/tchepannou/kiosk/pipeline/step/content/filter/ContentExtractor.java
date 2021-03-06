package io.tchepannou.kiosk.pipeline.step.content.filter;

import io.tchepannou.kiosk.pipeline.step.content.Filter;

import java.util.List;

public class ContentExtractor {
    private final List<Filter<String>> filters;

    public ContentExtractor(final List<Filter<String>> filters) {
        this.filters = filters;
    }

    public String extract(final String str) {
        String txt = str;
        for (final Filter<String> filter : filters) {
            txt = filter.filter(txt);
        }
        return txt;
    }
}
