package io.tchepannou.kiosk.core.nlp.tokenizer.impl;

import io.tchepannou.kiosk.core.nlp.tokenizer.TokenFilter;
import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;

import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isAlphabetic;
import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isDelimiter;
import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isHyphen;

public class BasicTokenizer implements Tokenizer {
    private final char[] ch;
    private int pos;
    private final int len;
    private final TokenFilter filter;

    public BasicTokenizer(final String str) {
        this(str, (s) -> true );
    }
    public BasicTokenizer(final String str, final TokenFilter filter) {
        ch = str.toCharArray();
        pos = 0;
        len = str.length();
        this.filter = filter;
    }

    @Override
    public String nextToken() {
        int offset = pos;
        boolean stop = false;
        while (pos < len && !stop) {
            final char cur = ch[pos];

            if (isDelimiter(cur)) {
                if (isHyphen(cur) && (pos - 1 >= 0 && isAlphabetic(ch[pos - 1])) && (pos + 1 < len && isAlphabetic(ch[pos + 1]))) {
                    pos++;
                } else if (pos > offset) { // End of word
                    stop = true;
                } else {
                    pos++;
                    stop = true;
                }
            } else {
                ++pos;
            }

            if (stop){
                final String text = new String(ch, offset, pos - offset);
                if (!filter.accept(text)){
                    stop = false;
                    offset = pos;
                }
            }
        }

        return pos > offset ? new String(ch, offset, pos - offset) : null;
    }

}
