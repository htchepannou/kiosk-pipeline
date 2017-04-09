package io.tchepannou.kiosk.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadCountDown {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadCountDown.class);
    private final AtomicInteger count = new AtomicInteger(0);
    private int sleepMillis = 30 * 1000;

    public void countUp() {
        count.incrementAndGet();
    }

    public void countDown() {
        count.decrementAndGet();
    }

    public void await() {
        while (count.get() > 0) {
            try {
                Thread.sleep(sleepMillis);
            } catch (final InterruptedException e) {
                return;
            }
        }

        LOGGER.info("All threads terminated");
    }

    public int getSleepMillis() {
        return sleepMillis;
    }

    public void setSleepMillis(final int sleepMillis) {
        this.sleepMillis = sleepMillis;
    }
}
