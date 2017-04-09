package io.tchepannou.kiosk.core.service.impl;

import io.tchepannou.kiosk.core.service.Delay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstantDelay implements Delay {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantDelay.class);

    private long maxWaitMillis;
    private long durationMillis;
    private long durationTotalMillis;

    public ConstantDelay() {
    }

    public ConstantDelay(final long durationMillis, final long maxWaitMillis) {
        this.durationMillis = durationMillis;
        this.maxWaitMillis = maxWaitMillis;
    }

    @Override
    public boolean sleep() throws InterruptedException {
        durationTotalMillis += durationMillis;
        LOGGER.info("Sleeping for {}. total wait={}", durationMillis, durationTotalMillis);

        Thread.sleep(durationMillis);
        return durationTotalMillis <= maxWaitMillis;
    }

    @Override
    public void reset() {
        durationTotalMillis = 0;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(final long durationMillis) {
        this.durationMillis = durationMillis;
    }
}
