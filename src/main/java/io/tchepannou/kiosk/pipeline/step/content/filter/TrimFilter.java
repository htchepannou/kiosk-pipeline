package io.tchepannou.kiosk.pipeline.step.content.filter;

import io.tchepannou.kiosk.pipeline.step.content.Filter;

public class TrimFilter implements Filter<String> {
    @Override
    public String filter(final String str) {
        final String html = str.trim();
        return html.startsWith("|") ? html.substring(1) : html;
    }
}
