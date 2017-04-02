package io.tchepannou.kiosk.core.service;

import java.io.IOException;
import java.util.List;

public interface MessageQueue {
    String getName();

    void push(final String msg) throws IOException;

    List<String> poll() throws IOException;
}
