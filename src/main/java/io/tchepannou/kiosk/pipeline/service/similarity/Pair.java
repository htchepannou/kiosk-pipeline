package io.tchepannou.kiosk.pipeline.service.similarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class Pair {
    private final long id1;
    private final long id2;
    private final double ratio;

    public Pair(final long id1, final long id2, final double ratio) {
        this.id1 = id1;
        this.id2 = id2;
        this.ratio = ratio;
    }

    public static List<Set<Long>> clusterize(Collection<Pair> pairs){
        List<Set<Long>> sets = new ArrayList<>();
        for (final Pair pair : pairs){
            sets.add(pair.toSet());
        }

        while(true){
            final List<Set<Long>> result = new ArrayList<>();
            clusterize(sets, result);
            if (sets.size() == result.size()){
                return result;
            }
            sets = result;
        }
    }

    private static void clusterize(Collection<Set<Long>> items, Collection<Set<Long>> clusters){
        for (final Set<Long> item : items){
            boolean disjoint = true;

            for (final Set<Long> cluster : clusters){
                if (!Collections.disjoint(cluster, item)){
                    cluster.addAll(item);
                    disjoint = false;
                    break;
                }
            }

            if (disjoint){
                clusters.add(item);
            }
        }
    }

    public long getId1() {
        return id1;
    }

    public long getId2() {
        return id2;
    }

    public double getRatio() {
        return ratio;
    }

    public Set<Long> toSet(){
        return new HashSet<>(Arrays.asList(id1, id2));
    }
}
