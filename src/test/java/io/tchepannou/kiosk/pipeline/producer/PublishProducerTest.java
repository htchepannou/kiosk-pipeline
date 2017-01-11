package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static io.tchepannou.kiosk.pipeline.Fixtures.createArticle;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PublishProducerTest {
    @Mock
    AmazonSQS sqs;

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

        producer.setOutputQueue("output-queue");
        producer.produce();

        verify(sqs).sendMessage("output-queue", String.valueOf(a1.getId()));
        verify(sqs).sendMessage("output-queue", String.valueOf(a2.getId()));
        verify(sqs).sendMessage("output-queue", String.valueOf(a3.getId()));
    }
}
