package io.tchepannou.kiosk.pipeline.step.validation;

public interface Rule <T> {
    Validation validate (T subject);
}
