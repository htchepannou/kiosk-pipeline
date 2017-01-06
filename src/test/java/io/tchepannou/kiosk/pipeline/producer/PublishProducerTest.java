package io.tchepannou.kiosk.pipeline.producer;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import org.assertj.core.util.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PublishProducerTest {
    @Mock
    ArticleRepository articleRepository;

    @InjectMocks
    PublishProducer producer;

    @Test
    public void testProduce() throws Exception {
        final Article a1 = createArticle();
        final Article a2 = createArticle();
        final Article a3 = createArticle();
        when(articleRepository.findByStatus(Article.STATUS_VALID)).thenReturn(Arrays.asList(a1, a2, a3));

        producer.produce();

        assertThat(a1.getStatus()).isEqualTo(Article.STATUS_PUBLISHED);
        assertThat(a2.getStatus()).isEqualTo(Article.STATUS_PUBLISHED);
        assertThat(a3.getStatus()).isEqualTo(Article.STATUS_PUBLISHED);

        ArgumentCaptor<Iterable> items = ArgumentCaptor.forClass(Iterable.class);
        verify(articleRepository).save(items.capture());

        List articles = Arrays.asList(Iterables.toArray(items.getValue()));
        assertThat(articles).containsExactly(a1, a2, a3);
    }

    private Article createArticle(){
        Link link = new Link();
        link.setUrl("http://goo.com/" + System.currentTimeMillis());

        Article article = new Article();
        article.setLink(link);
        return article;
    }

}
