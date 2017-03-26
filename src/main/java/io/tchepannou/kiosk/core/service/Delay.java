package io.tchepannou.kiosk.core.service;

public interface Delay {
    void reset();
    void sleep() throws InterruptedException;
}
