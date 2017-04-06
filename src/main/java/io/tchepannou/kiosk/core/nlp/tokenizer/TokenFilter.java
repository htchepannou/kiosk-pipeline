package io.tchepannou.kiosk.core.nlp.tokenizer;

public interface TokenFilter {
    boolean accept(String text);
}
