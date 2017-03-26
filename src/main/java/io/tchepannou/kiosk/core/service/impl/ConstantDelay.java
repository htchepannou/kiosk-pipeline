package io.tchepannou.kiosk.core.service.impl;

import io.tchepannou.kiosk.core.service.Delay;

public class ConstantDelay implements Delay {
    private long durationMillis;

    @Override
    public void sleep() throws InterruptedException {
        Thread.sleep(durationMillis);
    }

    @Override
    public void reset() {

    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(final long durationMillis) {
        this.durationMillis = durationMillis;
    }
}
