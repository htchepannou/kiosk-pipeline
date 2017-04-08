package io.tchepannou.kiosk.core.nlp.filter;

public class LowecaseTextFilter implements TextFilter {
    @Override
    public String filter(final String text) {
        return text.toLowerCase();
    }
}
