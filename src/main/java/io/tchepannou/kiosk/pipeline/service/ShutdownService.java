package io.tchepannou.kiosk.pipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public class ShutdownService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownService.class);

    @Autowired
    private ApplicationContext context;

    public void shutdown(final int exitCode) throws IOException {
        LOGGER.info("Shutting down...");
        SpringApplication.exit(context, () -> exitCode);
    }
}
