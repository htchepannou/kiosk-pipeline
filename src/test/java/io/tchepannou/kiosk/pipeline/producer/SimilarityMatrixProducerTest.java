package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.s3.AmazonS3;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.service.similarity.ArticleDocumentFactory;
import io.tchepannou.kiosk.pipeline.service.similarity.Document;
import io.tchepannou.kiosk.pipeline.service.similarity.SimilarityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Arrays;
import java.util.Date;

import static io.tchepannou.kiosk.pipeline.Fixtures.createArticle;
import static io.tchepannou.kiosk.pipeline.Fixtures.createDocument;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimilarityMatrixProducerTest {
    @Mock
    AmazonS3 s3;

    @Mock
    ArticleRepository articleRepository;

    @Mock
    ArticleDocumentFactory articleDocumentFactory;

    @Mock
    SimilarityService similarityService;

    @Mock
    Clock clock;

    @InjectMocks
    SimilarityMatrixProducer producer;

    @Before
    public void setUp (){
        producer.setS3Bucket("bucket");
        producer.setS3Key("dev/similarity");
    }

    @Test
    public void shouldGenerateMatrix() throws Exception {
        // Given
        final Date now = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse("2015/01/03 11:30");
        when(clock.millis()).thenReturn(now.getTime());

        final Article a1 = createArticle();
        final Article a2 = createArticle();
        final Article a3 = createArticle();
        when (articleRepository.findByStatusNotInAndPublishedDateBetween(anyList(), any(), any())).thenReturn(Arrays.asList(a1, a2, a3));

        final Document d1 = createDocument(a1.getId(), "doc1");
        final Document d2 = createDocument(a2.getId(), "doc2");
        final Document d3 = createDocument(a3.getId(), "doc3");
        when(articleDocumentFactory.createDocument(a1)).thenReturn(d1);
        when(articleDocumentFactory.createDocument(a2)).thenReturn(d2);
        when(articleDocumentFactory.createDocument(a3)).thenReturn(d3);

        // When
        producer.produce();

        // Then
        verify(s3).putObject(eq("bucket"), eq("dev/similarity/2015/01/03/matrix_11.txt"), any(File.class));
    }



}
