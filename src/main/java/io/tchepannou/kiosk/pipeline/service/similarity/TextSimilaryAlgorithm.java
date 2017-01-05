package io.tchepannou.kiosk.pipeline.service.similarity;

import java.util.Collection;

public interface TextSimilaryAlgorithm {
    float compute(final Collection<String> words1, final Collection<String> words2);
}
