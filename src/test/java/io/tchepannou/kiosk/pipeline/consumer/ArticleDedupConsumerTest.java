package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.service.similarity.Pair;
import io.tchepannou.kiosk.pipeline.service.similarity.SimilarityService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static io.tchepannou.kiosk.pipeline.Fixtures.createS3InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArticleDedupConsumerTest {
    @Mock
    AmazonS3 s3;

    @Mock
    AmazonSQS sqs;

    @Mock
    ArticleRepository articleRepository;

    @Mock
    SimilarityService similarityService;

    @InjectMocks
    ArticleDedupConsumer consumer;

    @Before
    public void setUp (){
        consumer.setInputQueue("input-queue");
    }

    @Test
    public void testConsume() throws Exception {
        // Given
        final Date now = new Date();
        final Article a11 = createArticle(11, now, Article.STATUS_VALID);
        final Article a12 = createArticle(12, DateUtils.addDays(now, 1), Article.STATUS_VALID);
        final Article a13 = createArticle(13, DateUtils.addDays(now, 2), Article.STATUS_VALID);
        final Article a21 = createArticle(21, now, Article.STATUS_VALID);
        final Article a22 = createArticle(22, DateUtils.addDays(now, 1), Article.STATUS_VALID);
        final Article a31 = createArticle(31, now, Article.STATUS_VALID);
        when(articleRepository.findAll(any(Iterable.class))).thenReturn(Arrays.asList(a11, a12, a13, a21, a22, a31));

        final Pair p1 = new Pair(11, 12, 0);
        final Pair p2 = new Pair(11, 13, 0);
        final Pair p3 = new Pair(21, 22, 0);
        when(similarityService.filter(any(InputStream.class), anyFloat(), anyFloat())).thenReturn(Arrays.asList(p1, p2, p3));

        final S3ObjectInputStream sin = createS3InputStream("toto");
        final S3Object obj = mock(S3Object.class);
        when(obj.getBucketName()).thenReturn("bucket");
        when(obj.getKey()).thenReturn("dev/foo/matrix_10.txt");
        when(obj.getObjectContent()).thenReturn(sin);

        // When
        consumer.consume(obj);

        // Then
        ArgumentCaptor<InputStream> in = ArgumentCaptor.forClass(InputStream.class);
        verify(s3).putObject(eq("bucket"), eq("dev/foo/matrix_10-dedup.txt"), in.capture(), any(ObjectMetadata.class));
        assertThat(IOUtils.toString(in.getValue())).isEqualTo(
                "11,12,13\n" +
                "21,22\n"
        );

        assertThat(a11.getStatus()).isEqualTo(Article.STATUS_VALID);
        assertThat(a12.getStatus()).isEqualTo(Article.STATUS_DUPLICATE);
        assertThat(a12.getDuplicateId()).isEqualTo(a11.getId());
        assertThat(a13.getStatus()).isEqualTo(Article.STATUS_DUPLICATE);
        assertThat(a13.getDuplicateId()).isEqualTo(a11.getId());

        assertThat(a21.getStatus()).isEqualTo(Article.STATUS_VALID);
        assertThat(a22.getStatus()).isEqualTo(Article.STATUS_DUPLICATE);
        assertThat(a22.getDuplicateId()).isEqualTo(a21.getId());

        ArgumentCaptor<Iterable> articles = ArgumentCaptor.forClass(Iterable.class);
        verify(articleRepository).save(articles.capture());

        List items = Arrays.asList(Iterables.toArray(articles.getValue()));
        assertThat(items).contains(a12, a13, a22);
    }

    Article createArticle(final long id, final Date publishedDate, final int status) {
        final Article article = new Article();
        article.setId(id);
        article.setPublishedDate(publishedDate);
        article.setStatus(status);
        return article;
    }
}
