package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.validation.Rule;
import io.tchepannou.kiosk.pipeline.step.validation.Validation;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleRule;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleShouldHaveMinContentLengthRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleShouldHaveMinContentLengthRuleTest {
    Rule rule = new ArticleShouldHaveMinContentLengthRule(200);

    @Test
    public void shouldAcceptArticleWithLargeContent() throws Exception {
        final Link article = new Link();
        article.setContentLength(300);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isTrue();
    }

    @Test
    public void shouldAcceptArticleWithMinContent() throws Exception {
        final Link article = new Link();
        article.setContentLength(200);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isTrue();
    }


    @Test
    public void shouldRejectArticleWithLowContent() throws Exception {
        final Link article = new Link();
        article.setContentLength(100);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isFalse();
        assertThat(validation.getReason()).isEqualTo(ArticleRule.CONTENT_TOO_SHORT);
    }
}
