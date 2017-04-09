package io.tchepannou.kiosk.core.nlp.filter;

public class LowercaseTextFilter implements TextFilter {
    @Override
    public String filter(final String text) {
        return text.toLowerCase();
    }
}
