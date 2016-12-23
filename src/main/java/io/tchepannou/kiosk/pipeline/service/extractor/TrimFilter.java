package io.tchepannou.kiosk.pipeline.service.extractor;

public class TrimFilter implements Filter<String> {
    @Override
    public String filter(final String str) {
        final String html = str.trim();
        return html.startsWith("|") ? html.substring(1) : html;
    }
}
