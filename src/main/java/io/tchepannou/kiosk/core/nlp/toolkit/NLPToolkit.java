package io.tchepannou.kiosk.core.nlp.toolkit;

import io.tchepannou.kiosk.core.nlp.tokenizer.StopWords;
import io.tchepannou.kiosk.core.nlp.tokenizer.Tokenizer;

public interface NLPToolkit {
    Tokenizer getTokenizer(final String text);
    StopWords getStopWords();
}
