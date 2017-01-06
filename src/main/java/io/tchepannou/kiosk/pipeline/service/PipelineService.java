package io.tchepannou.kiosk.pipeline.service;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsReader;
import io.tchepannou.kiosk.pipeline.consumer.ArticleDedupConsumer;
import io.tchepannou.kiosk.pipeline.producer.PublishProducer;
import io.tchepannou.kiosk.pipeline.producer.FeedProducer;
import io.tchepannou.kiosk.pipeline.producer.SimilarityMatrixProducer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class PipelineService {
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
        /* collect articles */
        feedProducer.produce();
        threadMonitor.waitAllThreads(60000, 60000 * 30);

        /* dedup */
        similarityMatrixProducer.produce();
        run(dedupConsumer.getInputQueue(), dedupConsumer);
        threadMonitor.waitAllThreads(60000, 60000 * 30);

        /* publish */
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
