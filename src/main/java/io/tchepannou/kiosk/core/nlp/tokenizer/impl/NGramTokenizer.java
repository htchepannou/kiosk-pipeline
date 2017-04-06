package io.tchepannou.kiosk.core.nlp.tokenizer.impl;

import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class NGramTokenizer implements Tokenizer {
    private final int min;
    private final int max;
    private final Tokenizer delegate;
    private final List<String> tokens = new ArrayList<>();
    private int pos = 0;

    public NGramTokenizer(final int n, final Tokenizer delegate) {
        this(1, n, delegate);
    }

    public NGramTokenizer(final int min, final int max, final Tokenizer delegate) {
        this.min = min;
        this.max = max;
        this.delegate = delegate;

        init();
    }

    @Override
    public String nextToken() {
        return pos < tokens.size() ? tokens.get(pos++) : null;
    }

    private void init() {
        // Load the token
        String token;
        final List<String> stream = new ArrayList<>();
        while ((token = delegate.nextToken()) != null) {
            stream.add(token);
        }

        // Extrapolate
        for (int i = 0; i < stream.size(); i++) {
            final StringBuilder sb = new StringBuilder();

            for (int j = 0; j < max; j++) {
                final int cur = i + j;
                if (cur >= stream.size()) {
                    break;
                }
                sb.append(stream.get(cur));

                if (j + 1 >= min) {
                    tokens.add(sb.toString());
                }
            }
        }
    }
}
