package io.tchepannou.kiosk.core.service;

import java.io.IOException;

public interface Consumer {
    void consume(String message) throws IOException;
}
