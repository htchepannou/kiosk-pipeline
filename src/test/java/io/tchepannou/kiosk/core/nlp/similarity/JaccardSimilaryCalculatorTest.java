package io.tchepannou.kiosk.core.nlp.similarity;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class JaccardSimilaryCalculatorTest {
    JaccardSimilaryCalculator service = new JaccardSimilaryCalculator();

    @Test
    public void testCompute() throws Exception {
        assertThat(service.compute(
                new HashSet(Arrays.asList("A B C", "B C D", "C D E", "D E F", "E F G")),
                new HashSet(Arrays.asList("A B C", "B C D", "C D E", "D E F", "E F G"))
        )).isEqualTo(1f);

        assertThat(service.compute(
                new HashSet(Arrays.asList("A B C", "B C D", "C D E", "D E F", "E F G", "F G H")),
                new HashSet(Arrays.asList("A B C", "C D E", "E F G"))
        )).isEqualTo(.5f);

        assertThat(service.compute(
                new HashSet(Arrays.asList("A B C", "B C D", "C D E", "D E F", "E F G", "F G H")),
                new HashSet(Arrays.asList("O P Q", "P Q R"))
        )).isEqualTo(0f);
    }


}
