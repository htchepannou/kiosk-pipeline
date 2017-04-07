package io.tchepannou.kiosk.core.nlp.tokenizer;

public class Delimiters {
    private static final String WHITESPACE = " \n\r\t";
    private static final char UNICODE_DOT = 8230;
    private static final char UNICODE_HYPHEN = 8211;
    private static final String FRAGMENT_DELIM = "\"«»“”.,?!;:()[]-" + UNICODE_HYPHEN + UNICODE_DOT;

    private static final String PUNCTUATION = ".,?!;:";
    private static final String DELIM = PUNCTUATION + WHITESPACE + FRAGMENT_DELIM;

    public static boolean isFragmentDelimiter(final String ch) {
        return ch != null && ch.length() == 1 && FRAGMENT_DELIM.contains(ch);
    }

    public static boolean isDelimiter(final String ch) {
        return ch != null && ch.length() == 1 && isDelimiter(ch.charAt(0));
    }

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

    public static boolean isWhitespace(final String ch) {
        return ch != null && ch.length() == 1 && isWhitespace(ch.charAt(0));
    }

    public static boolean isWhitespace(final char ch) {
        return WHITESPACE.indexOf(ch) >= 0;
    }
}
