package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kiosk.pipeline.FeedProducer")
public class FeedProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedProducer.class);

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    AmazonSQS sqs;

    private String outputQueue;

    public void produce() {
        final Iterable<Feed> feeds = feedRepository.findAll();
        for (final Feed feed : feeds) {
            try {
                LOGGER.info("Feed: {}", feed.getName());
                sqs.sendMessage(outputQueue, String.valueOf(feed.getId()));
            } catch (final Exception e) {
                LOGGER.error("Unable to process feed: {}", feed.getName(), e);
            }
        }
    }

    //-- Getter/Setter
    public String getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(final String outputQueue) {
        this.outputQueue = outputQueue;
    }
}
