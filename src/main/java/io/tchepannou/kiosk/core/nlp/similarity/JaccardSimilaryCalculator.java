package io.tchepannou.kiosk.core.nlp.similarity;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class JaccardSimilaryCalculator implements TextSimilaryCalculator {
    public float compute(final Collection<String> words1, final Collection<String> words2) {
        if (words1.isEmpty() && words2.isEmpty()) {
            return 1;
        }

        final Set<String> set1 = words1 instanceof Set ? (Set) words1 : new HashSet<>(words1);
        final Set<String> set2 = words2 instanceof Set ? (Set) words2 : new HashSet<>(words2);

        final Set<String> intersection = Sets.intersection(set1, set2);
        final Set<String> union = Sets.union(set1, set2);

        return (float) intersection.size() / (float) union.size();
    }

}
