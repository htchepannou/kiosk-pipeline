package io.tchepannou.kiosk.core.nlp.tokenizer;

import static io.tchepannou.kiosk.core.nlp.tokenizer.Delimiters.isDelimiter;

public class WordTokenizer implements Tokenizer {
    private final char[] ch;
    private int pos;
    private final int len;

    public WordTokenizer(final String str) {
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
                if (pos > offset) { // End of word
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
