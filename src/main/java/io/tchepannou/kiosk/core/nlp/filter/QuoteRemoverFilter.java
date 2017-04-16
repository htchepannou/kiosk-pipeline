package io.tchepannou.kiosk.core.nlp.filter;

public class QuoteRemoverFilter implements TextFilter {
    @Override
    public String filter(final String text) {
        return text.replaceAll("\"|»|«", "");
    }
}
