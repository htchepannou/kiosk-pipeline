package io.tchepannou.kiosk.pipeline.step.content;

public interface Filter<T> {
    T filter(T str);
}
