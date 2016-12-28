package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.service.validation.Validator;
import io.tchepannou.kiosk.pipeline.service.validation.article.ArticleShouldHaveContentRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class ValidationConfiguration {
    @Bean
    Validator<Article> articleValidator() {
        return new Validator<>(
                Arrays.asList(
                        articleShouldHaveContent()
                )
        );
    }

    @Bean
    ArticleShouldHaveContentRule articleShouldHaveContent() {
        return new ArticleShouldHaveContentRule();
    }
}
