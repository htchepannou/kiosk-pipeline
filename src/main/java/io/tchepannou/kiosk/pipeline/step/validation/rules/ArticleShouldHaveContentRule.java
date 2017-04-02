package io.tchepannou.kiosk.pipeline.step.validation.rules;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.validation.Validation;

public class ArticleShouldHaveContentRule implements ArticleRule {
    @Override
    public Validation validate(final Link subject) {
        return subject.getContentLength() == 0
                ? Validation.failure(NO_CONTENT)
                : Validation.success();
    }
}
