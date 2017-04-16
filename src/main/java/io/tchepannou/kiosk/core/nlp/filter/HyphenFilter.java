package io.tchepannou.kiosk.core.nlp.filter;

public class HyphenFilter implements TextFilter {
    @Override
    public String filter(final String text) {
        return text.replaceAll("-", " ");
    }
}
