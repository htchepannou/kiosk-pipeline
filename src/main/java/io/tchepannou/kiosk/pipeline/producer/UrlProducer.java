package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executor;

@Deprecated
public class UrlProducer {
    @Autowired
    UrlService urlService;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    Executor executor;

    @Autowired
    AmazonSQS sqs;

    private String outputQueue;

    //-- Public
    public void produce() {
        final Iterable<Feed> feeds = feedRepository.findAll();
        for (final Feed feed : feeds) {
            final Runnable worker = createWorker(feed);
            executor.execute(worker);
        }
    }


    private Runnable createWorker(final Feed feed){
        return new UrlProducerRunnable(feed, urlService, linkRepository, sqs, outputQueue);
    }

    //-- Getter/Setter
    public String getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(final String outputQueue) {
        this.outputQueue = outputQueue;
    }
}
