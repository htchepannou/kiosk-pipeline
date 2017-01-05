package io.tchepannou.kiosk.pipeline.service.similarity;

public class Dedup {
    private final long id1;
    private final long id2;
    private final double ratio;

    public Dedup(final long id1, final long id2, final double ratio) {
        this.id1 = id1;
        this.id2 = id2;
        this.ratio = ratio;
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
}
