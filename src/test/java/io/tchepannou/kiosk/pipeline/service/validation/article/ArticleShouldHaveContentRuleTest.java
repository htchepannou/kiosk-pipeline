package io.tchepannou.kiosk.pipeline.service.validation.article;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.validation.Rule;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleShouldHaveContentRuleTest {
    Rule rule = new ArticleShouldHaveContentRule();

    @Test
    public void shouldAcceptArticleWithContent() throws Exception {
        final Article article = new Article();
        article.setContentLength(100);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isTrue();
    }


    @Test
    public void shouldRejectArticleWithNoContent() throws Exception {
        final Article article = new Article();
        article.setContentLength(0);

        final Validation validation = rule.validate(article);

        assertThat(validation.isSuccess()).isFalse();
        assertThat(validation.getReason()).isEqualTo(ArticleRule.NO_CONTENT);
    }
}
