package io.tchepannou.kiosk.core.nlp.tokenizer.impl;

import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;

import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isAlphabetic;
import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isDelimiter;
import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isHyphen;

public class BasicTokenizer implements Tokenizer {
    private final char[] ch;
    private int pos;
    private final int len;

    public BasicTokenizer(final String str) {
        ch = str.toCharArray();
        pos = 0;
        len = str.length();
    }

    @Override
    public String nextToken() {
        final int offset = pos;
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
        }

        return pos > offset ? new String(ch, offset, pos - offset) : null;
    }

}
