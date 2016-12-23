package io.tchepannou.kiosk.pipeline.service.extractor;

public interface Filter<T> {
    T filter(T str);
}
