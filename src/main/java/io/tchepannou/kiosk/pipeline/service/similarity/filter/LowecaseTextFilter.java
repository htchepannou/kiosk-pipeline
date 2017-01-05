package io.tchepannou.kiosk.pipeline.service.similarity.filter;

import io.tchepannou.kiosk.pipeline.service.similarity.TextFilter;

public class LowecaseTextFilter implements TextFilter {
    @Override
    public String filter(final String text) {
        return text.toLowerCase();
    }
}
