package io.tchepannou.kiosk.core.nlp.tokenizer.impl;

import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;

import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isFragmentDelimiter;
import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isWhitespace;

public class FragmentTokenizer implements Tokenizer {
    private final Tokenizer delegate;

    public FragmentTokenizer(final Tokenizer delegate) {
        this.delegate = delegate;
    }

    @Override
    public String nextToken() {
        final StringBuilder sb = new StringBuilder();

        // First token
        String token;
        while (true) {
            token = delegate.nextToken();
            if (token == null) {
                return null;
            } else if (isFragmentDelimiter(token) || isWhitespace(token)) {
                continue;
            } else {
                sb.append(token);
                break;
            }
        }

        // Following tokens
        while (true) {
            token = delegate.nextToken();
            if (token == null || isFragmentDelimiter(token)) {
                break;
            } else {
                sb.append(token);
            }
        }
        return sb.toString().trim();
    }
}
