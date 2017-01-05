package io.tchepannou.kiosk.pipeline.service.similarity;

public class Dedup {
    private Document target;
    private double ratio;

    public Dedup(final Document target, final double ratio) {
        this.target = target;
        this.ratio = ratio;
    }

    public Document getTarget() {
        return target;
    }

    public double getRatio() {
        return ratio;
    }
}
