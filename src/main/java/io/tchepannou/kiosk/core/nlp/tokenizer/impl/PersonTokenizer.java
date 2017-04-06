package io.tchepannou.kiosk.core.nlp.tokenizer.impl;

import io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters;
import io.tchepannou.kiosk.core.nlp.tokenizer.TokenFilter;
import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;

public class PersonTokenizer implements Tokenizer {
    private final BasicTokenizer delegate;
    private final TokenFilter filter;

    public PersonTokenizer(final BasicTokenizer delegate, final TokenFilter filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public String nextToken() {
        final StringBuilder sb = new StringBuilder();
        int size = 0;

        while(true) {
            final String token = delegate.nextToken();

            if (token == null){     // end of stream
                break;
            } else if (Delimiters.isWhitespace(token)) {
                continue;
            } else if (!filter.accept(token)){
                if (size > 0){   // end of token
                    break;
                }
            } else {
                if (Character.isUpperCase(token.charAt(0))){
                    if (sb.length()>0){
                        sb.append(' ');
                    }
                    sb.append(token);
                    size++;
                } else {
                    if (size > 0){   // end of token
                        break;
                    }
                }
            }
        }

        final String result = sb.toString().trim();
        return result.length() > 0 ? result : null;
    }
}
