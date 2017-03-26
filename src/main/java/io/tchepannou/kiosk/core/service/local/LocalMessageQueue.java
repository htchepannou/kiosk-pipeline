package io.tchepannou.kiosk.core.service.local;

import io.tchepannou.kiosk.core.service.MessageQueue;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalMessageQueue implements MessageQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMessageQueue.class);

    private String home;
    private int pollMaxSize = 10;

    @Override
    public void push(final String msg) throws IOException {
        // Create directory
        final File dir = new File(home);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Write the file
        final File file = new File(dir, UUID.randomUUID().toString() + ".txt");
        try (final OutputStream out = new FileOutputStream(file)) {
            LOGGER.info("{}: pushing {}", getName(), msg);
            final InputStream in = new ByteArrayInputStream(msg.getBytes());
            IOUtils.copy(in, out);
        }
    }

    @Override
    public List<String> poll() throws IOException {
        final List<String> messages = new ArrayList<>();
        final File[] files = new File(home).listFiles();
        if (files != null) {
            for (final File file : files) {
                try (InputStream in = new FileInputStream(file)) {
                    final String msg = IOUtils.toString(in);
                    messages.add(msg);
                    if (messages.size() >= pollMaxSize) {
                        break;
                    }
                } finally {
                    file.delete();
                }
            }
        }
        return messages;
    }

    @Override
    public String getName() {
        return new File(home).getName();
    }

    public String getHome() {
        return home;
    }

    public void setHome(final String home) {
        this.home = home;
    }

    public int getPollMaxSize() {
        return pollMaxSize;
    }

    public void setPollMaxSize(final int pollMaxSize) {
        this.pollMaxSize = pollMaxSize;
    }
}
