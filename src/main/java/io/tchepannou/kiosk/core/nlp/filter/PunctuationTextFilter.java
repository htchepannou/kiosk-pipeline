package io.tchepannou.kiosk.core.nlp.filter;

public class PunctuationTextFilter implements TextFilter {
    @Override
    public String filter(final String text) {
        return text.replaceAll("\\p{Punct}", " ");
    }
}
