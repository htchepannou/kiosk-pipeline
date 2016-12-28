package io.tchepannou.kiosk.pipeline.consumer;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;
import io.tchepannou.kiosk.pipeline.service.validation.Validator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArticleValidationConsumerTest {
    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private Validator articleValidator;

    @InjectMocks
    private ArticleValidationConsumer consumer;

    @Before
    public void setUp (){
        consumer.setInputQueue("input-queue");
    }

    @Test
    public void shouldAcceptValidArticle() throws Exception {
        // Given
        final Article article = new Article();
        when(articleRepository.findOne(123L)).thenReturn(article);

        when(articleValidator.validate(any())).thenReturn(Validation.success());

        // When
        consumer.consume("123");

        // Then
        verify(articleRepository).save(article);
        assertThat(article.getStatus()).isEqualTo(Article.STATUS_VALID);
        assertThat(article.getInvalidReason()).isNull();
    }

    @Test
    public void shouldRejectInvalidArticle() throws Exception {
        // Given
        final Article article = new Article();
        when(articleRepository.findOne(123L)).thenReturn(article);

        when(articleValidator.validate(any())).thenReturn(Validation.failure("error"));

        // When
        consumer.consume("123");

        // Then
        verify(articleRepository).save(article);
        assertThat(article.getStatus()).isEqualTo(Article.STATUS_INVALID);
        assertThat(article.getInvalidReason()).isEqualTo("error");
    }
}
