package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;

public class ArticleShouldHaveContentRule implements ArticleRule {
    @Override
    public Validation validate(final Article subject) {
        return subject.getContentLength() == 0
                ? Validation.failure(ArticleRule.NO_CONTENT)
                : Validation.success();
    }
}
