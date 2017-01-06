package io.tchepannou.kiosk.pipeline.service.similarity;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PairTest {

    @Test
    public void testClusterize() throws Exception {
        final Pair p1 = new Pair(1,2,0);
        final Pair p2 = new Pair(2,3,0);
        final Pair p3 = new Pair(4,5,0);
        final Pair p4 = new Pair(1,6,0);
        final Pair p5 = new Pair(10,11,0);
        final Pair p6 = new Pair(5,13,0);

        List<Set<Long>> clusters = Pair.clusterize(Arrays.asList(p1,p2,p3,p4,p5,p6));

        assertThat(clusters).hasSize(3);
        assertThat(clusters.get(0)).containsExactly(1L, 2L, 3L, 6L);
        assertThat(clusters.get(1)).containsExactly(4L, 5L, 13L);
        assertThat(clusters.get(2)).containsExactly(10L, 11L);
    }
}
