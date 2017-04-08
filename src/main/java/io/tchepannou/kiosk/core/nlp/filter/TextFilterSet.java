package io.tchepannou.kiosk.core.nlp.filter;

import java.util.List;

public class TextFilterSet implements TextFilter {
    private final List<TextFilter> filters;

    public TextFilterSet(final List<TextFilter> filters) {
        this.filters = filters;
    }

    @Override
    public String filter(final String text) {
        String xtext = text;
        for (TextFilter filter : filters){
            if (xtext == null){
                break;
            }
            xtext = filter.filter(xtext);
        }
        return xtext;
    }
}
