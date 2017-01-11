package io.tchepannou.kiosk.pipeline.service;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumerGroup;
import io.tchepannou.kiosk.pipeline.producer.FeedProducer;
import io.tchepannou.kiosk.pipeline.producer.PublishProducer;
import io.tchepannou.kiosk.pipeline.producer.SimilarityMatrixProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.io.IOException;

public class PipelineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineRunner.class);

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

    @Autowired
    @Qualifier("AquisitionConsumers")
    SqsConsumerGroup aquisitionConsumers;

    @Autowired
    @Qualifier("DedupConsumers")
    SqsConsumerGroup dedupConsumers;

    @Autowired
    @Qualifier("PublishConsumers")
    SqsConsumerGroup publishConsumers;

    @Value("${kiosk.pipeline.autoStart}")
    public boolean autoStart;

    //-- Public
    @PostConstruct
    public void init () throws IOException {
        if (autoStart){
            run();
        }
    }

    public void run() throws IOException {
//        fetch();
//        dedup();
        publish();
    }

    //-- Private
    private void fetch() {
        feedProducer.produce();
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

    private void publish (){
        publishProducer.produce();
        publishConsumers.consume();
    }
}
