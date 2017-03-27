package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.validation.Rule;
import io.tchepannou.kiosk.pipeline.step.validation.Validation;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleRule;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleShouldHaveTitleRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleShouldHaveTitleRuleTest {
    Rule rule = new ArticleShouldHaveTitleRule();

    @Test
    public void shouldAcceptArticleWithTitle() throws Exception {
        final Link article = new Link();
        article.setTitle("This is a sample title");

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isTrue();
    }

    @Test
    public void shouldRejectArticleWithNullTitle() throws Exception {
        final Link article = new Link();
        article.setTitle(null);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isFalse();
        assertThat(validation.getReason()).isEqualTo(ArticleRule.NO_TITLE);
    }

    @Test
    public void shouldRejectArticleWithEmptyTitle() throws Exception {
        final Link article = new Link();
        article.setTitle("");

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isFalse();
        assertThat(validation.getReason()).isEqualTo(ArticleRule.NO_TITLE);
    }
}
