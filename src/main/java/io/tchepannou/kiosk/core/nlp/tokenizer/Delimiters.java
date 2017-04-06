package io.tchepannou.kiosk.core.nlp.tokenizer;

public class Delimiters {
    private static final String PUNCTUATION = ".,?!;:";
    private static final String DELIM = PUNCTUATION + "'\"’-+/* \n\r\t«»“”()[]";

    public static boolean isHyphen(final char ch) {
        return ch == '-';
    }

    public static boolean isAlphabetic(final char ch) {
        return Character.isAlphabetic(ch);
    }

    public static boolean isDelimiter(final char ch) {
        return DELIM.indexOf(ch) >= 0;
    }

    public static boolean isPunctuation(final char ch) {
        return PUNCTUATION.indexOf(ch) >= 0;
    }

    public static boolean isPunctuation(final String ch) {
        return ch != null && ch.length() == 1 && isPunctuation(ch.charAt(0));
    }
}
