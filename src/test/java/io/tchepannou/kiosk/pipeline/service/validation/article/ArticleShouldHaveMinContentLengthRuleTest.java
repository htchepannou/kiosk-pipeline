package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.validation.Rule;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleShouldHaveMinContentLengthRuleTest {
    Rule rule = new ArticleShouldHaveMinContentLengthRule(200);

    @Test
    public void shouldAcceptArticleWithLargeContent() throws Exception {
        final Article article = new Article();
        article.setContentLength(300);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isTrue();
    }

    @Test
    public void shouldAcceptArticleWithMinContent() throws Exception {
        final Article article = new Article();
        article.setContentLength(200);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isTrue();
    }


    @Test
    public void shouldRejectArticleWithLowContent() throws Exception {
        final Article article = new Article();
        article.setContentLength(100);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isFalse();
        assertThat(validation.getReason()).isEqualTo(ArticleRule.CONTENT_TOO_SHORT);
    }
}
