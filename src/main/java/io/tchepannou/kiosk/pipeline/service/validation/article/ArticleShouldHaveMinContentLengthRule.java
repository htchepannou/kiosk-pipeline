package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;

public class ArticleShouldHaveMinContentLengthRule implements ArticleRule {
    private final int minContentLength;

    public ArticleShouldHaveMinContentLengthRule(final int minContentLength) {
        this.minContentLength = minContentLength;
    }

    @Override
    public Validation validate(final Article subject) {
        return subject.getContentLength() < minContentLength
                ? Validation.failure(ArticleRule.CONTENT_TOO_SHORT)
                : Validation.success();
    }
}
