package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.UrlBlacklistService;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.tchepannou.kiosk.pipeline.Fixtures.createArticle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArticleUrlShouldNotBeBlacklistedRuleTest {
    @Mock
    UrlBlacklistService service;

    @InjectMocks
    ArticleUrlShouldNotBeBlacklistedRule rule;

    @Test
    public void shouldAcceptNonBlacklistesUrl() throws Exception {
        final Article article = createArticle();
        when(service.contains(article.getLink().getUrl())).thenReturn(false);

        final Validation result = rule.validate(article);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void shouldNotAcceptBlacklistesUrl() throws Exception {
        final Article article = createArticle();
        when(service.contains(article.getLink().getUrl())).thenReturn(true);

        final Validation result = rule.validate(article);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getReason()).isEqualTo(ArticleRule.BLACKLISTED);
    }
}
