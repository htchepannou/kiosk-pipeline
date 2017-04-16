package io.tchepannou.kiosk.pipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executor;

public class ShutdownService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownService.class);

    @Autowired
    Executor executor;

    public void shutdownNow(){
        shutdown(0);
    }

    public void shutdown(final int sleepMillis) {
        executor.execute(() -> {
            try {
                if (sleepMillis > 0) {
                    Thread.sleep(sleepMillis);
                }

                LOGGER.info("Shutting down...");
                System.exit(0);
            } catch (final InterruptedException e) {

            }
        });
    }

}
