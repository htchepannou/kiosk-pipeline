package io.tchepannou.kiosk.core.nlp.tokenizer;

public class Delimiters {
    private static final String PUNCTUATION = ".,?!;:";
    private static final String WHITESPACE = " \n\r\t";
    private static final String DELIM = PUNCTUATION + WHITESPACE + "'\"’-+/*«»“”()[]";

    public static boolean isDelimiter(final String ch){
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
