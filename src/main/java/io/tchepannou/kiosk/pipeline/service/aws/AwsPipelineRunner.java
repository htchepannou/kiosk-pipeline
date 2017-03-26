package io.tchepannou.kiosk.pipeline.service.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumerGroup;
import io.tchepannou.kiosk.pipeline.consumer.ArticleContentExtractorConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.producer.PublishProducer;
import io.tchepannou.kiosk.pipeline.producer.SimilarityMatrixProducer;
import io.tchepannou.kiosk.pipeline.producer.UrlProducer;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.io.IOException;

public class AwsPipelineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsPipelineRunner.class);

    @Autowired
    AmazonSQS sqs;

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    ThreadMonitor threadMonitor;

    @Autowired
    UrlProducer urlProducer;

    @Autowired
    SimilarityMatrixProducer similarityMatrixProducer;

    @Autowired
    PublishProducer publishProducer;

    @Autowired
    @Qualifier("AquisitionConsumers")
    SqsConsumerGroup aquisitionConsumers;

    @Autowired
    @Qualifier("DedupConsumers")
    SqsConsumerGroup dedupConsumers;

    @Autowired
    @Qualifier("PublishConsumers")
    SqsConsumerGroup publishConsumers;

    @Autowired
    private ArticleRepository articleRepository;

    @Value("${kiosk.pipeline.reprocess}")
    private String reprocess;

    //-- Public
    @PostConstruct
    public void init() throws IOException {
        if ("extract-content".equals(reprocess)) {
            extractContent();
        }
    }

    public void run() throws IOException {
        fetch();
        dedup();
        publish();
    }

    public void extractContent() throws IOException {
        final String queue = applicationContext.getBean(ArticleContentExtractorConsumer.class).getInputQueue();
        final Iterable<Article> articles = articleRepository.findAll();
        for (final Article article : articles) {
            final long id = article.getId();

            LOGGER.info("Sending <{}> to <{}>", id, queue);
            sqs.sendMessage(queue, String.valueOf(id));
        }
    }

    //-- Private
    private void fetch() {
        urlProducer.produce();
        aquisitionConsumers.consume();
    }

    private void dedup() {
        try {
            similarityMatrixProducer.produce();
            dedupConsumers.consume();
        } catch (final IOException e) {
            LOGGER.warn("Unable to filter dedup", e);
        }
    }

    private void publish() {
        publishProducer.produce();
        publishConsumers.consume();
    }
}
