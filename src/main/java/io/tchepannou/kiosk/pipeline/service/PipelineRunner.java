package io.tchepannou.kiosk.pipeline.service;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumerGroup;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsReader;
import io.tchepannou.kiosk.pipeline.consumer.ArticleContentExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleDedupConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleMetadataConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleValidationConsumer;
import io.tchepannou.kiosk.pipeline.consumer.HtmlDownloadConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageMainConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageThumbnailConsumer;
import io.tchepannou.kiosk.pipeline.consumer.UrlExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.VideoExtractorConsumer;
import io.tchepannou.kiosk.pipeline.producer.FeedProducer;
import io.tchepannou.kiosk.pipeline.producer.PublishProducer;
import io.tchepannou.kiosk.pipeline.producer.SimilarityMatrixProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class PipelineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineRunner.class);
    private static final int CONSUMER_THREADS = 10;

    @Autowired
    AmazonSQS sqs;

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    ThreadMonitor threadMonitor;

    @Autowired
    FeedProducer feedProducer;

    @Autowired
    SimilarityMatrixProducer similarityMatrixProducer;

    @Autowired
    PublishProducer publishProducer;


    //-- Public
    public void run() throws IOException {
        fetchArticles();
        filterOutDedup();
        publish();
    }

    //-- Private
    private void fetchArticles() {
        feedProducer.produce();
        createAquisitionConsumers().run();
    }

    private void filterOutDedup() {
        try {
            similarityMatrixProducer.produce();
            createDedupConsumers().run();
        } catch (IOException e){
            LOGGER.warn("Unable to filter dedup", e);
        }
    }

    private void publish() {
        publishProducer.produce();
    }

    private SqsConsumerGroup createAquisitionConsumers() {
        final SqsConsumerGroup group = new SqsConsumerGroup(sqs, threadMonitor, applicationContext);
        group.add(UrlExtractorConsumer.class, CONSUMER_THREADS);
        group.add(HtmlDownloadConsumer.class, CONSUMER_THREADS);

        group.add(ArticleContentExtractorConsumer.class, CONSUMER_THREADS);
        group.add(ArticleMetadataConsumer.class, CONSUMER_THREADS);
        group.add(ArticleValidationConsumer.class, CONSUMER_THREADS);

        group.add(ImageExtractorConsumer.class, CONSUMER_THREADS);
        group.add(ImageThumbnailConsumer.class, 2 * CONSUMER_THREADS);
        group.add(ImageMainConsumer.class, 2 * CONSUMER_THREADS);

        group.add(VideoExtractorConsumer.class, 2 * CONSUMER_THREADS);

        return group;
    }

    private SqsConsumerGroup createDedupConsumers() {
        final SqsConsumerGroup group = new SqsConsumerGroup(sqs, threadMonitor, applicationContext);
        group.add(ArticleDedupConsumer.class, CONSUMER_THREADS);
        return group;
    }

    private void run(final String queue, final SqsConsumer consumer) {
        new SqsReader(
                queue,
                sqs,
                consumer,
                threadMonitor
        ).run();
    }
}
