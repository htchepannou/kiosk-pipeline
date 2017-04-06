package io.tchepannou.kiosk.core.nlp.tokenizer;

import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isPunctuation;

public class SentenceTokenizer implements Tokenizer {
    private final Tokenizer delegate;

    public SentenceTokenizer(final Tokenizer delegate) {
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
            } else if (Delimiters.isPunctuation(token)) {
                continue;
            } else {
                sb.append(token);
                break;
            }
        }

        // Following tokens
        while (true){
            token = delegate.nextToken();
            if (token == null || isPunctuation(token)) {
                break;
            } else {
                sb.append(token);
            }
        }
        return sb.toString().trim();
    }
}
