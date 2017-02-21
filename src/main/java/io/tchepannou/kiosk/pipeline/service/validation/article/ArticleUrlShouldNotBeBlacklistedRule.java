package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;
import org.springframework.beans.factory.annotation.Autowired;

public class ArticleUrlShouldNotBeBlacklistedRule implements ArticleRule {
    @Autowired
    UrlService service;

    @Override
    public Validation validate(final Article subject) {
        final String url = subject.getLink().getUrl();
        return service.isBlacklisted(url) ? Validation.failure(ArticleRule.BLACKLISTED) : Validation.success();
    }
}
