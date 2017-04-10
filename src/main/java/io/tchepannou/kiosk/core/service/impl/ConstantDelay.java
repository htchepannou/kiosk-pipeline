package io.tchepannou.kiosk.core.service.impl;

import io.tchepannou.kiosk.core.service.Delay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstantDelay implements Delay {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantDelay.class);

    private long maxSleepMillis;
    private long sleepMillis;
    private long totalSleepMillis;

    public ConstantDelay() {
    }

    public ConstantDelay(final long sleepMillis, final long maxSleepMillis) {
        this.sleepMillis = sleepMillis;
        this.maxSleepMillis = maxSleepMillis;
    }

    @Override
    public boolean sleep() throws InterruptedException {
        totalSleepMillis += sleepMillis;
        LOGGER.info("Sleeping for {}. total wait={}", sleepMillis, totalSleepMillis);

        Thread.sleep(sleepMillis);
        return totalSleepMillis <= maxSleepMillis;
    }

    @Override
    public void reset() {
        totalSleepMillis = 0;
    }

    public long getMaxSleepMillis() {
        return maxSleepMillis;
    }

    public void setMaxSleepMillis(final long maxSleepMillis) {
        this.maxSleepMillis = maxSleepMillis;
    }

    public long getSleepMillis() {
        return sleepMillis;
    }

    public void setSleepMillis(final long sleepMillis) {
        this.sleepMillis = sleepMillis;
    }

    public long getTotalSleepMillis() {
        return totalSleepMillis;
    }
}
