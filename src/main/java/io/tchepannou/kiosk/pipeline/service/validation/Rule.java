package io.tchepannou.kiosk.pipeline.service.validation;

public interface Rule <T> {
    Validation validate (T subject);
}
