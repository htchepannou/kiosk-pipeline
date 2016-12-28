package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.validation.Rule;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleShouldHaveTitleRuleTest {
    Rule rule = new ArticleShouldHaveContentRule();

    @Test
    public void shouldAcceptArticleWithTitle() throws Exception {
        final Article article = new Article();
        article.setTitle("This is a sample title");

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isTrue();
    }

    @Test
    public void shouldRejectArticleWithNullTitle() throws Exception {
        final Article article = new Article();
        article.setTitle(null);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isFalse();
        assertThat(validation.getReason()).isEqualTo(ArticleRule.NO_TITLE);
    }

    @Test
    public void shouldRejectArticleWithEmptyTitle() throws Exception {
        final Article article = new Article();
        article.setTitle("");

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isFalse();
        assertThat(validation.getReason()).isEqualTo(ArticleRule.NO_TITLE);
    }
}
