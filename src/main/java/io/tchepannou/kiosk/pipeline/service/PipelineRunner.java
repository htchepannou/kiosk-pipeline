package io.tchepannou.kiosk.pipeline.service;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsReader;
import io.tchepannou.kiosk.pipeline.consumer.ArticleDedupConsumer;
import io.tchepannou.kiosk.pipeline.producer.PublishProducer;
import io.tchepannou.kiosk.pipeline.producer.FeedProducer;
import io.tchepannou.kiosk.pipeline.producer.SimilarityMatrixProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class PipelineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineRunner.class);

    @Autowired
    AmazonSQS sqs;

    @Autowired
    FeedProducer feedProducer;

    @Autowired
    SimilarityMatrixProducer similarityMatrixProducer;

    @Autowired
    PublishProducer publishProducer;

    @Autowired
    ArticleDedupConsumer dedupConsumer;

    @Autowired
    ThreadMonitor threadMonitor;

    public void run() throws IOException {
        fetchArticles();
        filterOutDedup();
        publish();
    }

    private void fetchArticles(){
        feedProducer.produce();
        threadMonitor.waitAllThreads(60000, 60000 * 30);
    }

    private void filterOutDedup(){
        try {
            similarityMatrixProducer.produce();
            run(dedupConsumer.getInputQueue(), dedupConsumer);
            threadMonitor.waitAllThreads(60000, 60000 * 30);
        } catch (Exception e){
            LOGGER.warn("Unexpected error when filtering dedub", e);
        }
    }

    private void publish (){
        publishProducer.produce();
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
