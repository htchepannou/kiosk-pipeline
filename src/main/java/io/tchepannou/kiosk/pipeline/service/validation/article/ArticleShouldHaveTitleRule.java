package io.tchepannou.kiosk.pipeline.service.validation.article;

import com.google.common.base.Strings;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;

public class ArticleShouldHaveTitleRule implements ArticleRule{
    @Override
    public Validation validate(final Article subject) {
        return Strings.isNullOrEmpty(subject.getTitle())
                ? Validation.failure(ArticleRule.NO_CONTENT)
                : Validation.success();
    }
}
