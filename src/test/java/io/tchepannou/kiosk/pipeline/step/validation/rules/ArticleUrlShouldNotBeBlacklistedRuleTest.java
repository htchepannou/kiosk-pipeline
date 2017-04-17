package io.tchepannou.kiosk.pipeline.step.validation.rules;

import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import io.tchepannou.kiosk.pipeline.step.validation.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArticleUrlShouldNotBeBlacklistedRuleTest {
    @Mock
    UrlService service;

    @InjectMocks
    ArticleUrlShouldNotBeBlacklistedRule rule;

    @Test
    public void shouldAcceptNonBlacklistesUrl() throws Exception {
        final Link article = new Link();
        when(service.isBlacklisted(article.getUrl())).thenReturn(false);

        final Validation result = rule.validate(article);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldNotAcceptBlacklistesUrl() throws Exception {
        final Link article = new Link();
        when(service.isBlacklisted(article.getUrl())).thenReturn(true);

        final Validation result = rule.validate(article);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getReason()).isEqualTo(ArticleRule.BLACKLISTED);
    }
}
