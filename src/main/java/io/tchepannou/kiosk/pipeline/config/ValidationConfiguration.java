package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.validation.Validator;
import io.tchepannou.kiosk.pipeline.service.validation.article.ArticleShouldHaveContentRule;
import io.tchepannou.kiosk.pipeline.service.validation.article.ArticleShouldHaveMinContentLengthRule;
import io.tchepannou.kiosk.pipeline.service.validation.article.ArticleShouldHaveTitleRule;
import io.tchepannou.kiosk.pipeline.service.validation.article.ArticleUrlShouldNotBeBlacklistedRule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@ConfigurationProperties("kiosk.validation")
public class ValidationConfiguration {
    private int minContextLength;

    @Bean
    Validator<Article> articleValidator() {
        return new Validator<>(
                Arrays.asList(
                        articleShouldHaveContent(),
                        articleShouldHaveMinContentRule(),
                        articleShouldHaveTitleRule(),
                        articleUrlShouldNotBeBlacklistedRule()
                )
        );
    }

    @Bean
    ArticleShouldHaveContentRule articleShouldHaveContent() {
        return new ArticleShouldHaveContentRule();
    }

    @Bean
    ArticleShouldHaveMinContentLengthRule articleShouldHaveMinContentRule(){
        return new ArticleShouldHaveMinContentLengthRule(minContextLength);
    }

    @Bean
    ArticleShouldHaveTitleRule articleShouldHaveTitleRule(){
        return new ArticleShouldHaveTitleRule();
    }

    @Bean
    ArticleUrlShouldNotBeBlacklistedRule articleUrlShouldNotBeBlacklistedRule(){
        return new ArticleUrlShouldNotBeBlacklistedRule();
    }

    public int getMinContextLength() {
        return minContextLength;
    }

    public void setMinContextLength(final int minContextLength) {
        this.minContextLength = minContextLength;
    }
}
