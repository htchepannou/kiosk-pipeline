package io.tchepannou.kiosk.pipeline.service.content;

public interface Filter<T> {
    T filter(T str);
}
