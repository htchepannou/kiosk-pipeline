package io.tchepannou.kiosk.pipeline.step.url;

import io.tchepannou.kiosk.core.service.Producer;
import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.persistence.repository.FeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class UrlProducer implements Producer{
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlProducer.class);

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    FeedUrlProducer feedUrlProducer;

    @Override
    public void produce() {
        final Iterable<Feed> feeds = feedRepository.findAll();
        for (final Feed feed : feeds) {
            try {
                feedUrlProducer.produce(feed);
            } catch (Exception e){
                LOGGER.error("Unable to produce URL from {}", feed.getUrl(), e);
            }
        }
    }
}
