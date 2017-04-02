package io.tchepannou.kiosk.pipeline.service;

import java.io.IOException;

public class InvalidContentTypeException extends IOException {
    public InvalidContentTypeException() {
    }

    public InvalidContentTypeException(final String message) {
        super(message);
    }
}
