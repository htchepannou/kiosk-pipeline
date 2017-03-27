package io.tchepannou.kiosk.pipeline.step.validation.rules;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import io.tchepannou.kiosk.pipeline.step.validation.Validation;
import org.springframework.beans.factory.annotation.Autowired;

public class ArticleUrlShouldNotBeBlacklistedRule implements ArticleRule {
    @Autowired
    UrlService service;

    @Override
    public Validation validate(final Link subject) {
        final String url = subject.getUrl();
        return service.isBlacklisted(url) ? Validation.failure(BLACKLISTED) : Validation.success();
    }
}
