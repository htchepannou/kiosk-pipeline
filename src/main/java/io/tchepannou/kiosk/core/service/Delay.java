package io.tchepannou.kiosk.core.service;

public interface Delay {
    void reset();
    boolean sleep() throws InterruptedException;
}
