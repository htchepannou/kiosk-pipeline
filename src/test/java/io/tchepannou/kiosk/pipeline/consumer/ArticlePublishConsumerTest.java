package io.tchepannou.kiosk.pipeline.consumer;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.tchepannou.kiosk.pipeline.Fixtures.createArticle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArticlePublishConsumerTest {
    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    ArticlePublishConsumer consumer;

    @Test
    public void testConsume() throws Exception {
        final Article a = createArticle();
        when(articleRepository.findOne(123L)).thenReturn(a);

        consumer.consume("123");

        assertThat(a.getStatus()).isEqualTo(Article.STATUS_PUBLISHED);

        verify(articleRepository).save(a);

    }
}
