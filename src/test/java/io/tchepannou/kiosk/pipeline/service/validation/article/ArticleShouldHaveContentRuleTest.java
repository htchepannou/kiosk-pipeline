package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.validation.Rule;
import io.tchepannou.kiosk.pipeline.step.validation.Validation;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleRule;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleShouldHaveContentRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleShouldHaveContentRuleTest {
    Rule rule = new ArticleShouldHaveContentRule();

    @Test
    public void shouldAcceptArticleWithContent() throws Exception {
        final Link article = new Link();
        article.setContentLength(100);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isTrue();
    }

    @Test
    public void shouldRejectArticleWithNoContent() throws Exception {
        final Link article = new Link();
        article.setContentLength(0);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isFalse();
        assertThat(validation.getReason()).isEqualTo(ArticleRule.NO_CONTENT);
    }
}
