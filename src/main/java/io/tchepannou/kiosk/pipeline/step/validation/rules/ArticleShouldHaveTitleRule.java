package io.tchepannou.kiosk.pipeline.step.validation.rules;

import com.google.common.base.Strings;
import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.validation.Validation;

public class ArticleShouldHaveTitleRule implements ArticleRule{
    @Override
    public Validation validate(final Link subject) {
        return Strings.isNullOrEmpty(subject.getTitle())
                ? Validation.failure(NO_TITLE)
                : Validation.success();
    }
}
