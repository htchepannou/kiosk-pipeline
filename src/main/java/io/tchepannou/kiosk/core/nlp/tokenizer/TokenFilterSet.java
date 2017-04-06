package io.tchepannou.kiosk.core.nlp.tokenizer;

import java.util.List;

public class TokenFilterSet implements TokenFilter{
    private final List<TokenFilter> filters;

    public TokenFilterSet(final List<TokenFilter> filters) {
        this.filters = filters;
    }

    @Override
    public boolean accept(final String text) {
        for (final TokenFilter filter : filters){
            if (!filter.accept(text)){
                return false;
            }
        }
        return true;
    }
}
