package io.tchepannou.kiosk.core.nlp.similarity;

import java.util.Collection;

public interface TextSimilaryCalculator {
    float compute(final Collection<String> words1, final Collection<String> words2);
}
