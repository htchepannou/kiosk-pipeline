package io.tchepannou.kiosk.pipeline.step.validation.rules;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.validation.Validation;

public class ArticleShouldHaveMinContentLengthRule implements ArticleRule {
    private final int minContentLength;

    public ArticleShouldHaveMinContentLengthRule(final int minContentLength) {
        this.minContentLength = minContentLength;
    }

    @Override
    public Validation validate(final Link subject) {
        return subject.getContentLength() < minContentLength
                ? Validation.failure(CONTENT_TOO_SHORT)
                : Validation.success();
    }
}
